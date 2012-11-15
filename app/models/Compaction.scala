/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import play.api.Logger
import org.apache.hadoop.hbase.ServerName
import play.api.libs.ws.WS
import collection.mutable.MutableList
import java.util.Date
import utils.HBaseConnection

case class Compaction(region: String, start: Date, end: Date)

object Compaction extends HBaseConnection {

  val STARTING = "Starting"
  val COMPETED = "completed"

  val COMPACTION = """(.*) INFO compactions.CompactionRequest: completed compaction: regionName=(.*\.), storeName=(.*), fileCount=(.*), fileSize=(.*), priority=(.*), time=(.*); duration=(.*)sec""".r

  def init() = {
    Logger.info("setting Loglevels to INFO for the Regionservers")
    eachServer {
      (hbaseAdmin, clusterStatus, serverName) =>
        val url = baseUrl(serverName) + "/logLevel?log=org.apache.hadoop.hbase&level=INFO"
        WS.url(url).get().map {
          response =>
            Logger.debug("... Loglevel set to INFO for server " + serverName.getHostname())
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

    eachServer {
      (hbaseAdmin, clusterStatus, serverName) =>
        val hostName = serverName.getHostname()
        val url = baseUrl(serverName) + "/logs/hbase-hbase-regionserver-" + hostName + ".log"
        Logger.debug("... fetching Logfile from " + url)
        val response = WS.url(url).get().value.get
        if(response.ahcResponse.getStatusCode() != 200) {
           throw new Exception("couldn't load Compaction Metrics from URL: " + url);
        }

        // TODO: this pattern-matching is so damn slow, replace by something faster!
        for (COMPACTION(date, region, store, fileCount, fileSize, priority, time, duration) <- COMPACTION findAllIn response.body) {
          val date = new java.util.Date(time.toLong / 1000 / 1000)
          val durationMsec = if (duration.toLong > 0) {
            duration.toLong * 1000
          } else {
            1
          }
          resultList += Compaction(region, new Date(date.getTime() - durationMsec), date)
        }
    }
    resultList.toList
  }

  def baseUrl(serverName: ServerName) = "http://" + serverName.getHostname() + ":60030"
}
