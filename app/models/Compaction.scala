/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import play.api.Logger
import org.apache.hadoop.hbase.HServerInfo
import play.api.libs.ws.{Response, WS}
import collection.mutable.MutableList
import java.util.Date
import utils.HBaseConnection
import java.text.SimpleDateFormat
import java.util.regex._
import collection.mutable
import play.api.libs.concurrent.NotWaiting
import org.apache.commons.lang.StringUtils

case class Compaction(region: String, start: Date, end: Date)

object Compaction extends HBaseConnection {
  val NEWLINE = "\n".getBytes("UTF-8")(0)

  val STARTING = "Starting"
  val COMPETED = "completed"

  val COMPACTION = Pattern.compile(
    """^(.*) INFO (.*).HRegion: (Starting|completed) compaction on region (.*\.)""",
    Pattern.MULTILINE
  )

  var logFetchTimeout: Int = 5
  var initialLogLookBehindSizeInKBs: Long = 1024
  var logFileUrlPattern: String = null
  var logLevelUrlPattern: String = null
  var setLogLevelsOnStartup: Boolean = false
  var logFileDateFormat: SimpleDateFormat = null
  var logOffsets = mutable.Map.empty[HServerInfo, Long]

  def configure(setLogLevelsOnStartup: Boolean = false,
                logFileUrlPattern: String = null,
                logLevelUrlPattern: String = null,
                logFileDateFormat: String = null,
                logFetchTimeout: Int = 5,
                initialLookBehindSizeInKBs: Long = 1024) = {
    this.setLogLevelsOnStartup = setLogLevelsOnStartup
    this.logFileUrlPattern = logFileUrlPattern
    this.logLevelUrlPattern = logLevelUrlPattern
    this.logFileDateFormat = new java.text.SimpleDateFormat(logFileDateFormat)
    this.logFetchTimeout = logFetchTimeout
    this.initialLogLookBehindSizeInKBs = initialLogLookBehindSizeInKBs
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
        try
        {
          val url = logFileUrl(serverInfo)
          var startPoints = Map[String, Date]()
          if (!logOffsets.contains(serverInfo)) {
            val headValue: NotWaiting[Response] = WS.url(url).head().value
            val logLength = headValue.get.header("Content-Length").get.toLong
            val offset = scala.math.max(0, logLength - initialLogLookBehindSizeInKBs * 1024)
            Logger.info("Initializing log offset to [%d] for log file at %s with content-length [%d]".format(offset, url, logLength))
            logOffsets(serverInfo) = offset
          }

          var response: Response = recentLogContent(url, serverInfo, logOffsets(serverInfo))

          val contentRange = response.getAHCResponse.getHeader("Content-Range")
          val rangeValue = StringUtils.substringBetween(contentRange, "bytes", "/").trim()
          if (rangeValue eq "*") {
            // The offset doesn't match the file content because, presumably, of log file rotation,
            // reset the offset to 0
            logOffsets(serverInfo) = 0l
            Logger.info("Log file [%s] seems to have rotated, resetting offset to 0".format(url))
            response = recentLogContent(url, serverInfo, logOffsets(serverInfo))
          } else {
            // Set the next offset to the base offset + the offset matching the last newline found
            logOffsets(serverInfo) = logOffsets(serverInfo) + offsetOfLastNewline(response.body)

            Logger.debug("Updating logfile offset to [%d] for server [%s]".
              format(logOffsets(serverInfo), serverInfo.getServerName))
          }
 
          val m = COMPACTION.matcher(response.body)
          while(m.find()) {
            val date = m.group(1)
            val pkg = m.group(2)
            val typ = m.group(3)
            val region = m.group(4)

            if (typ == STARTING) {
              try {
                startPoints += region -> logFileDateFormat.parse(date)
              } catch {
                case e: Exception => throw new Exception("Couldn't parse the date '" + date + "' with dateformat '" +
                  logFileDateFormat.toPattern + "', please check compactions.logfile-date-format in application.conf")
              }
            } else {
              if (!startPoints.contains(region)) {
                Logger.info("... no compaction-start found for compaction on region: " + region)
              } else {
                val startDate = startPoints(region)
                var endDate = logFileDateFormat.parse(date)
                if(endDate.getTime() < startDate.getTime()) {
                  endDate = new Date(startDate.getTime()+1)
                }
                resultList += Compaction(region, startDate, endDate)
                startPoints -= region
              }
            }
          }

          if (startPoints.size > 0) Logger.info("... " + startPoints.size + " compactions currently running on " + serverInfo.getHostname())
        }
        catch
        {
          case e:java.util.concurrent.TimeoutException => throw new Exception("'" + e.getMessage()
            + "' please try to increase compactions.logfile-fetch-timeout-in-seconds in application.conf")
          case e:java.text.ParseException => throw new Exception("'" + e.getMessage()
            + "' please check compactions.logfile-date-format in application.conf");
        }
    }
    resultList.toList
  }

  def offsetOfLastNewline(body: String):Long = {
    val bytes: Array[Byte] = body.getBytes

    for (i <- bytes.length - 1 to  0 by -1) {
      if (bytes(i) == NEWLINE) {
        return i
      }
    }

    0
  }

  def recentLogContent(url: String, serverInfo: HServerInfo, offset: Long) = {
    Logger.debug("... fetching Logfile from %s with range [%d-]".format(url, offset))
    val response = WS.url(url).withHeaders(("Range", "bytes=%d-".format(offset))).get().await(logFetchTimeout * 1000).get
    if (!List(200, 206).contains(response.ahcResponse.getStatusCode)) {
      throw new Exception("couldn't load Compaction Metrics from URL: '" +
        url + "', please check compactions.logfile_pattern in application.conf")
    }

    response
  }

  def logFileUrl(serverInfo: HServerInfo) = fillPlaceholders(serverInfo, logFileUrlPattern)

  def logLevelUrl(serverInfo: HServerInfo) = fillPlaceholders(serverInfo, logLevelUrlPattern)

  def fillPlaceholders(serverInfo: HServerInfo, string: String) =
    string
      .replaceAll("%hostname%", serverInfo.getHostname())
      .replaceAll("%infoport%", serverInfo.getInfoPort().toString)
      .replaceAll("%hostname-without-domain%", serverInfo.getHostname().split("\\.")(0))
}
