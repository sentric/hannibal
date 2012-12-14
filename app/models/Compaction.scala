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
import java.text.SimpleDateFormat
import java.util.regex._

case class Compaction(region: String, start: Date, end: Date)

object Compaction extends HBaseConnection {

  val COMPACTION = Pattern.compile(
    """^(.*) INFO (.*)\.CompactionRequest: completed compaction: regionName=(.*\.), storeName=(.*), fileCount=(.*), fileSize=(.*), priority=(.*), time=(.*); duration=(.*)$""",
    Pattern.MULTILINE
  )
  val TIME = Pattern.compile("""^((\d+)mins, )?((\d+)sec)$""")

  var logFetchTimeout: Int = 5
  var logFileUrlPattern: String = null
  var logLevelUrlPattern: String = null
  var setLogLevelsOnStartup: Boolean = false
  var logFileDateFormat: SimpleDateFormat = null

  def configure(setLogLevelsOnStartup: Boolean = false,
                logFileUrlPattern: String = null,
                logLevelUrlPattern: String = null,
                logFileDateFormat: String = null,
                logFetchTimeout: Int = 5) = {
    this.setLogLevelsOnStartup = setLogLevelsOnStartup
    this.logFileUrlPattern = logFileUrlPattern
    this.logLevelUrlPattern = logLevelUrlPattern
    this.logFileDateFormat = new java.text.SimpleDateFormat(logFileDateFormat)
    this.logFetchTimeout = logFetchTimeout
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
        val response = WS.url(url).get().await(logFetchTimeout * 1000).get
        if (response.ahcResponse.getStatusCode() != 200) {
          throw new Exception("couldn't load Compaction Metrics from URL: '" +
            url + "', please check compactions.logfile_pattern in application.conf");
        }

        try
        {
          val m = COMPACTION.matcher(response.body);
          while(m.find()) {
            val date = m.group(1)
            val pkg = m.group(2)
            val region = m.group(3)
            val store = m.group(4)
            val fileCount = m.group(5)
            val fileSize = m.group(6)
            val priority = m.group(7)
            val time = m.group(8)
            val duration = m.group(9)

            val end = logFileDateFormat.parse(date)
            val durationMsec = parseDuration(duration)

            resultList += Compaction(region, new Date(end.getTime() - durationMsec), end)
          }
        }
        catch
        {
          case e:java.text.ParseException => throw new Exception("'" + e.getMessage()
            + "' please check compactions.logfile-date-format in application.conf");
        }
    }
    resultList.toList
  }

  def parseDuration(s:String) = {
    val m = TIME.matcher(s)
    m.find()
    var seconds = m.group(4).toLong
    if (m.group(2) != null)
      seconds += m.group(2).toLong * 60

    if(seconds > 0) {
      seconds * 1000
    } else {
       1
    }
  }

  def logFileUrl(serverName: ServerName) = fillPlaceholders(serverName, logFileUrlPattern)

  def logLevelUrl(serverName: ServerName) = fillPlaceholders(serverName, logLevelUrlPattern)

  def fillPlaceholders(serverName: ServerName, string: String) =
    string
      .replaceAll("%hostname%", serverName.getHostname())
      .replaceAll("%infoport%", "60030")
      .replaceAll("%hostname-without-domain%", serverName.getHostname().split("\\.")(0))
}
