/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import play.api.Logger
import org.apache.hadoop.hbase.HServerInfo
import play.api.libs.ws.WS
import collection.mutable.{MutableList, Map}
import java.util.Date
import utils.HBaseConnection

case class Compaction(region: String, start: Date, end: Date)

object Compaction extends HBaseConnection {

  val STARTING = "Starting"
  val COMPETED = "completed"

  val COMPACTION = """(.*) INFO org.apache.hadoop.hbase.regionserver.HRegion: (Starting|completed) compaction on region (.*\.)""".r
  val DATE_FORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS")

  def init() = {
    Logger.info("setting Loglevels to INFO for the Regionservers")
    eachServerInfo {
      serverInfo =>
        val url = baseUrl(serverInfo) + "/logLevel?log=org.apache.hadoop.hbase&level=INFO"
        WS.url(url).get().map {
          response =>
            Logger.debug("... Loglevel set to INFO for server " + serverInfo.getHostname())
        }
    }
  }

  def forRegion(compactions: Seq[Compaction], region: String): Seq[Compaction] = {
    compactions.filter((compaction) => {
      compaction.region == region
    })
  }

  def all(): Seq[Compaction] = {
    var resultList = MutableList[Compaction]()
    var result: String = null
    eachServerInfo {
      serverInfo =>
        val hostName = serverInfo.getHostname().split("\\.")(0)
        val url = baseUrl(serverInfo) + "/logs/hbase-hbase-regionserver-" + hostName + ".log"
        Logger.debug("... fetching Logfile from " + url)
        result = WS.url(url).get().value.get.body

        var startPoints = Map[String, Date]()

        // TODO: this pattern-matching is so damn slow, replace by something faster!
        for (COMPACTION(date, typ, region) <- COMPACTION findAllIn result) {
          if (typ == STARTING) {
            startPoints += region -> DATE_FORMAT.parse(date)
          } else {
            if (!startPoints.contains(region)) {
              Logger.info("... no compaction-start found for compaction on region: " + region)
            } else {
              resultList += Compaction(region, startPoints(region), DATE_FORMAT.parse(date))
              startPoints -= region
            }
          }
        }
        if(startPoints.size > 0) Logger.info("... " + startPoints.size + " compactions currently running on "+ hostName)
    }
    resultList.toList
  }

  def baseUrl(serverInfo: HServerInfo) = "http://" + serverInfo.getHostname() + ":" + serverInfo.getInfoPort()
}