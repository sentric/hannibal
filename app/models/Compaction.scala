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
      eachServer {
        (hbaseAdmin, clusterStatus, serverName) =>
          val url = logLevelUrl(serverName)
          val response = WS.url(url).get().value.get
          if (response.ahcResponse.getStatusCode() != 200) {
            throw new Exception("couldn't set log-level with URL: " + url);
          } else {
            Logger.debug("... Loglevel set for server " + serverName.getHostname())
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
    eachServer {
      (hbaseAdmin, clusterStatus, serverName) =>
        val url = logFileUrl(serverName)
        Logger.debug("... fetching Logfile from " + url)
        val response = WS.url(url).get().value.get
        if (response.ahcResponse.getStatusCode() != 200) {
          throw new Exception("couldn't load Compaction Metrics from URL: " + url + " check compactions.logfile_pattern in application.conf");
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

  def logFileUrl(serverName: ServerName) = fillPlaceholders(serverName, logFileUrlPattern)

  def logLevelUrl(serverName: ServerName) = fillPlaceholders(serverName, logLevelUrlPattern)

  def fillPlaceholders(serverName: ServerName, string: String) =
    string
      .replaceAll("%hostname%", serverName.getHostname())
      .replaceAll("%infoport%", "60030")
      .replaceAll("%hostname-without-domain%", serverName.getHostname().split("\\.")(0))
}
