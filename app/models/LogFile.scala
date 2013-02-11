/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import collection.mutable
import play.api.Logger
import play.api.libs.ws.{Response, WS}
import play.api.libs.concurrent.NotWaiting
import org.apache.commons.lang.StringUtils
import models.LogFile._
import collection.mutable.ListBuffer
import scala.util.control.Breaks._
import java.util.regex._

case class LogFile(regionServer:RegionServer) {

  def tail() = {
    val url = logFileUrl(regionServer)

    if (!logOffsets.contains(regionServer.serverName)) {
      val headValue: NotWaiting[Response] = WS.url(url).head().value
      val logLength = headValue.get.header("Content-Length").get.toLong
      val offset = scala.math.max(0, logLength - initialLogLookBehindSizeInKBs * 1024)
      Logger.info("Initializing log offset to [%d] for log file at %s with content-length [%d]".format(offset, url, logLength))
      logOffsets(regionServer.serverName) = offset
    }

    var response: Response = recentLogContent(url, logOffsets(regionServer.serverName))

    val contentRange = response.getAHCResponse.getHeader("Content-Range")
    val rangeValue = StringUtils.substringBetween(contentRange, "bytes", "/").trim()
    if (rangeValue eq "*") {
      // The offset doesn't match the file content because, presumably, of log file rotation,
      // reset the offset to 0
      logOffsets(regionServer.serverName) = 0l
      Logger.info("Log file [%s] seems to have rotated, resetting offset to 0".format(url))
      response = recentLogContent(url, logOffsets(regionServer.serverName))
    } else {
      // Set the next offset to the base offset + the offset matching the last newline found
      logOffsets(regionServer.serverName) = logOffsets(regionServer.serverName) + offsetOfLastNewline(response.body)

      Logger.debug("Updating logfile offset to [%d] for server %s".
        format(logOffsets(regionServer.serverName), regionServer))
    }

    response.body
  }

  def recentLogContent(url: String, offset: Long) = {
    Logger.debug("... fetching Logfile from %s with range [%d-]".format(url, offset))
    val response = WS.url(url).withHeaders(("Range", "bytes=%d-".format(offset))).get().await(logFetchTimeout * 1000).get
    val ahcResponse = response.ahcResponse
    val statusCode = ahcResponse.getStatusCode
    if (!List(200, 206).contains(statusCode)) {
        throw new Exception("couldn't load Compaction Metrics from URL: '" +
          url + " (statusCode was: "+statusCode+")")
    }

    response
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
}

object LogFile {

  private var logFetchTimeout: Int = 5
  private var initialLogLookBehindSizeInKBs: Long = 1024
  private var logFileUrlPattern: String = null
  private var logFilePathPattern: Pattern = null
  private var logLevelUrlPattern: String = null
  private var setLogLevelsOnStartup: Boolean = false
  private var logFileDateFormat: SimpleDateFormat = null
  private var logOffsets = mutable.Map.empty[String, Long]

  val NEWLINE = "\n".getBytes("UTF-8")(0)

  def configure(setLogLevelsOnStartup: Boolean = false,
                logLevelUrlPattern: String = null,
                logFilePathPattern: String = null,
                logFileDateFormat: String = null,
                logFetchTimeout: Int = 5,
                initialLookBehindSizeInKBs: Long = 1024) = {
    this.setLogLevelsOnStartup = setLogLevelsOnStartup
    this.logLevelUrlPattern = logLevelUrlPattern
    this.logFilePathPattern = Pattern.compile(logFilePathPattern)
    this.logFileDateFormat = new java.text.SimpleDateFormat(logFileDateFormat)
    this.logFetchTimeout = logFetchTimeout
    this.initialLogLookBehindSizeInKBs = initialLogLookBehindSizeInKBs
  }

  def discoverLogFileUrlPattern = {
    var logFilePattern: String  = null
    breakable {
      HBase.eachRegionServer {
        regionServer =>
          val url = logRootUrl(regionServer)
          val response = WS.url(url).get().value.get
          val logFileMatcher = logFilePathPattern.matcher(response.body)

          if (logFileMatcher.find()) {
            val path = logFileMatcher.group(1)
            // We assume that all region servers use the same pattern so once we've got the pattern for one of them,
            // we stop
            Logger.info("Found path matching compactions.logfile-path-pattern: %s".format(path))
            logFilePattern = (url + path).replaceAll(regionServer.hostName, "%hostname%")
              .replaceAll(regionServer.infoPort.toString, "%infoport%")
              .replaceAll(regionServer.hostName.split("\\.")(0), "%hostname-without-domain%")
            break()
          }
      }
    }

    logFilePattern
  }

  def init() {
    if (setLogLevelsOnStartup) {
      Logger.info("setting Loglevels for the Regionservers")
      HBase.eachRegionServer {
        regionServer =>
          val url = logLevelUrl(regionServer)
          val response = WS.url(url).get().value.get
          if (response.ahcResponse.getStatusCode() != 200) {
            throw new Exception("couldn't set log-level with URL: " + url);
          } else {
            Logger.debug("... Loglevel set for server %s".format(regionServer))
          }
      }
    }

    logFileUrlPattern = discoverLogFileUrlPattern
    Logger.info("Discovered log file url pattern: [%s]".format(logFileUrlPattern))
  }

  def all() = {
    val list = new ListBuffer[LogFile]()
    HBase.eachRegionServer {
      regionServer =>
        list += LogFile(regionServer)
    }
    list.toList
  }

  def dateFormat() = logFileDateFormat

  def forServer(regionServer: RegionServer) = LogFile(regionServer)

  def logFileUrl(regionServer: RegionServer) = regionServer.infoUrl(logFileUrlPattern)

  def logLevelUrl(regionServer: RegionServer) = regionServer.infoUrl(logLevelUrlPattern)

  def logRootUrl(regionServer: RegionServer) = regionServer.infoUrl("http://%hostname%:%infoport%/logs/")
}
