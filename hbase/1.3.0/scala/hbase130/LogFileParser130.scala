/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package models.hbase130

import models.LogFile
import java.util.regex._
import java.util.Date
import models.hbase.LogFileParser

class LogFileParser130 extends LogFileParser {

  var COMPACTION = Pattern.compile(
    """^(.*) INFO (.*).HStore: Completed (.*)compaction of (.*) of (.*\.) into (.*) and took (.*) to execute(.*)$""",
    Pattern.MULTILINE
  )
  var DATE_GROUP = 1
  var REGION_GROUP = 5
  var DURATION_GROUP = 7

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

  override def setOverrideCopactionRegexPattern(pattern: Pattern): Unit = {
    COMPACTION = pattern
  }

  override def setDateGroupPosition(pos: Int): Unit = {
    DATE_GROUP = pos
  }

  override def setRegionGroupPosition(pos: Int): Unit = {
    REGION_GROUP = pos
  }

  override def setDurationGroupPosition(pos: Int): Unit = {
    DURATION_GROUP = pos
  }
}
