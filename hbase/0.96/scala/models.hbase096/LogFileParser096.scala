/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package models.hbase096

import models.LogFile
import java.util.regex._
import java.util.Date
import models.hbase.LogFileParser

class LogFileParser096 extends LogFileParser {

  val COMPACTION = Pattern.compile(
    """^(.*) INFO (.*).HStore: Completed (.*)compaction of (.*) of (.*\.) into (.*) and took (.*) to execute(.*)$""",
    Pattern.MULTILINE
  )
  val DATE_GROUP = 1
  val REGION_GROUP = 5
  val DURATION_GROUP = 7

  override def eachCompaction(logFile: LogFile, functionBlock: (String, Date, Long) => Unit) = {
    val m = COMPACTION.matcher(logFile.tail())
    while(m.find()) {
      val date = m.group(DATE_GROUP)
      val region = m.group(REGION_GROUP)
      val duration = m.group(DURATION_GROUP)

      val end = LogFile.dateFormat.parse(date)
      val durationMsec = parseDuration(duration)

      functionBlock(region, end, durationMsec)
    }
  }
}
