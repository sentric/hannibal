/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import play.api.Logger
import collection.mutable.MutableList
import java.util.Date
import collection.mutable

case class Compaction(region: String, start: Date, end: Date)

object Compaction {
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
          HBase.logFileParser.eachCompaction (logFile, { (region:String, end:Date, duration:Long) =>
            resultList += Compaction(region, new Date(end.getTime() - duration), end)
          })
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
