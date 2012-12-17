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

import java.util.regex._
import collection.mutable
import play.api.libs.concurrent.NotWaiting
import org.apache.commons.lang.StringUtils

case class Compaction(region: String, start: Date, end: Date)

object Compaction {

  val TIME = Pattern.compile("""^((\d+)mins, )?((\d+)sec)$""")

  // The Pattern is dependent on the HBase-Version - between HBase 0.90 and HBase 0.92 the format was changed
  val COMPACTION = Pattern.compile(
    """^(.*) INFO (.*).HRegion: completed compaction on region (.*\.) after (.*)$""",
    Pattern.MULTILINE
  )
  val DATE_GROUP = 1
  val REGION_GROUP = 3
  val DURATION_GROUP = 4

  def forRegion(compactions: Seq[Compaction], region: String): Seq[Compaction] = {
    compactions.filter((compaction) => {
      compaction.region == region
    })
  }

  def all(): Seq[Compaction] = {
    var resultList = MutableList[Compaction]()
    LogFile.all().foreach {
      logFile =>
        try {
          var startPoints = Map[String, Date]()
          val m = COMPACTION.matcher(logFile.tail())
          while(m.find()) {
            val date = m.group(DATE_GROUP)
            val region = m.group(REGION_GROUP)
            val duration = m.group(DURATION_GROUP)

            val end = LogFile.dateFormat.parse(date)
            val durationMsec = parseDuration(duration)

            resultList += Compaction(region, new Date(end.getTime() - durationMsec), end)
          }

          if (startPoints.size > 0) Logger.info("... " + startPoints.size + " compactions currently running on " + logFile.regionServer.serverName)
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

}
