/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import play.api.Logger
import org.apache.hadoop.hbase.ServerName
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

  val COMPACTION = Pattern.compile(
    """^(.*) INFO (.*)\.CompactionRequest: completed compaction: regionName=(.*\.), storeName=(.*), fileCount=(.*), fileSize=(.*), priority=(.*), time=(.*); duration=(.*)$""",
    Pattern.MULTILINE
  )
  val TIME = Pattern.compile("""^((\d+)mins, )?((\d+)sec)$""")

  var logFetchTimeout: Int = 5
  var initialLogLookBehindSizeInKBs: Long = 1024
  var logFileUrlPattern: String = null
  var logLevelUrlPattern: String = null
  var setLogLevelsOnStartup: Boolean = false
  var logFileDateFormat: SimpleDateFormat = null
  var logOffsets = mutable.Map.empty[ServerName, Long]

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
        try
        {
          val url = logFileUrl(serverName)
          if (!logOffsets.contains(serverName)) {
            val headValue: NotWaiting[Response] = WS.url(url).head().value
            val logLength = headValue.get.header("Content-Length").get.toLong
            val offset = scala.math.max(0, logLength - initialLogLookBehindSizeInKBs * 1024)
            Logger.info("Initializing log offset to [%d] for log file at %s with content-length [%d]".format(offset, url, logLength))
            logOffsets(serverName) = offset
          }

          var response: Response = recentLogContent(url, serverName, logOffsets(serverName))

          val contentRange = response.getAHCResponse.getHeader("Content-Range")
          val rangeValue = StringUtils.substringBetween(contentRange, "bytes", "/").trim()
          if (rangeValue eq "*") {
            // The offset doesn't match the file content because, presumably, of log file rotation,
            // reset the offset to 0
            logOffsets(serverName) = 0l
            Logger.info("Log file [%s] seems to have rotated, resetting offset to 0".format(url))
            response = recentLogContent(url, serverName, logOffsets(serverName))
          } else {
            // Set the next offset to the base offset + the offset matching the last newline found
            logOffsets(serverName) = logOffsets(serverName) + offsetOfLastNewline(response.body)

            Logger.debug("Updating logfile offset to [%d] for server [%s]".
              format(logOffsets(serverName), serverName.getServerName))
          }
 
          val m = COMPACTION.matcher(response.body)
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

  def recentLogContent(url: String, serverName: ServerName, offset: Long) = {
    Logger.debug("... fetching Logfile from %s with range [%d-]".format(url, offset))
    val response = WS.url(url).withHeaders(("Range", "bytes=%d-".format(offset))).get().await(logFetchTimeout * 1000).get
    if (!List(200, 206).contains(response.ahcResponse.getStatusCode)) {
      throw new Exception("couldn't load Compaction Metrics from URL: '" +
        url + "', please check compactions.logfile_pattern in application.conf")
    }

    response
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
