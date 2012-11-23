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

  var logFileUrlPattern: String = null
  var logLevelUrlPattern: String = null
  var setLogLevelsOnStartup: Boolean = false

  def configure(setLogLevelsOnStartup: Boolean = false, logFileUrlPattern: String = null, logLevelUrlPattern: String = null) = {
    this.setLogLevelsOnStartup = setLogLevelsOnStartup
    this.logFileUrlPattern = logFileUrlPattern
    this.logLevelUrlPattern = logLevelUrlPattern
  }

  def init() = {
    if (setLogLevelsOnStartup) {
      Logger.info("setting Loglevels for the Regionservers")
      eachServerInfo {
        serverInfo =>
          val url = logLevelUrl(serverInfo)
          val response = WS.url(url).get().value.get
          if (response.ahcResponse.getStatusCode() != 200) {
            throw new Exception("couldn't set log-level with URL: " + url);
          } else {
            Logger.debug("... Loglevel set for server " + serverInfo.getHostname())
          }
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
    eachServerInfo {
      serverInfo =>
        val url = logFileUrl(serverInfo)
        Logger.debug("... fetching Logfile from " + url)
        val response = WS.url(url).get().value.get
        if (response.ahcResponse.getStatusCode() != 200) {
          throw new Exception("couldn't load Compaction Metrics from URL: " + url + " check compactions.logfile_pattern in application.conf");
        }

        var startPoints = Map[String, Date]()

        // TODO: this pattern-matching is so damn slow, replace by something faster!
        for (COMPACTION(date, typ, region) <- COMPACTION findAllIn response.body) {
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
        if (startPoints.size > 0) Logger.info("... " + startPoints.size + " compactions currently running on " + serverInfo.getHostname())
    }
    resultList.toList
  }

  def logFileUrl(serverInfo: HServerInfo) = fillPlaceholders(serverInfo, logFileUrlPattern)

  def logLevelUrl(serverInfo: HServerInfo) = fillPlaceholders(serverInfo, logLevelUrlPattern)

  def fillPlaceholders(serverInfo: HServerInfo, string: String) =
    string
      .replaceAll("%hostname%", serverInfo.getHostname())
      .replaceAll("%infoport%", serverInfo.getInfoPort().toString)
      .replaceAll("%hostname-without-domain%", serverInfo.getHostname().split("\\.")(0))
}
