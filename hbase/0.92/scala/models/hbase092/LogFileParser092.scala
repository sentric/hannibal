/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package models.hbase092

import models.LogFile
import java.util.regex._
import java.util.Date
import models.hbase.LogFileParser

class LogFileParser092 extends LogFileParser {

  val COMPACTION = Pattern.compile(
    """(.*) \[.*\] (.*) - completed compaction: regionName=(.*\.), storeName=(.*), fileCount=(.*), fileSize=(.*), priority=(.*), time=(.*); duration=(.*)$""",
    Pattern.MULTILINE
  )
  val DATE_GROUP = 1
  val REGION_GROUP = 3
  val DURATION_GROUP = 9

  override def eachCompaction(logFile: LogFile, functionBlock: (String, Date, Long) => Unit) = {
    val t = logFile.tail()
    val m = COMPACTION.matcher(t)
    while(m.find()) {
      val date = m.group(DATE_GROUP)
      val regionRaw = m.group(REGION_GROUP)
      // Our logs are inconsistent and sometimes encode the region as 
      // "tableName,,id" instead of "tableName,startKey,id", so we'll strip out 
      // the startKey when it's present
      val pattern = ",.*,".r
      val region = pattern replaceFirstIn(regionRaw, ",,")
      val duration = m.group(DURATION_GROUP)

      val end = LogFile.dateFormat.parse(date)
      val durationMsec = parseDuration(duration)
      functionBlock(region, end, durationMsec)
    }
  }

}
