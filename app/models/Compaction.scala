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
  val STARTING = "Starting"
  val COMPETED = "completed"

  val COMPACTION = Pattern.compile(
    """^(.*) INFO (.*).HRegion: (Starting|completed) compaction on region (.*\.)""",
    Pattern.MULTILINE
  )

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
            val date = m.group(1)
            val pkg = m.group(2)
            val typ = m.group(3)
            val region = m.group(4)

            if (typ == STARTING) {
              try {
                startPoints += region -> LogFile.dateFormat.parse(date)
              } catch {
                case e: Exception => throw new Exception("Couldn't parse the date '" + date + "' with dateformat '" +
                  LogFile.dateFormat.toPattern + "', please check compactions.logfile-date-format in application.conf")
              }
            } else {
              if (!startPoints.contains(region)) {
                Logger.info("... no compaction-start found for compaction on region: " + region)
              } else {
                val startDate = startPoints(region)
                var endDate = LogFile.dateFormat.parse(date)
                if(endDate.getTime() < startDate.getTime()) {
                  endDate = new Date(startDate.getTime()+1)
                }
                resultList += Compaction(region, startDate, endDate)
                startPoints -= region
              }
            }
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

}
