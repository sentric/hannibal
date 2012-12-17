package models

import java.util.Date
import java.util.regex._

trait LogFileParser {

  val TIME = Pattern.compile("""^((\d+)mins, )?((\d+)sec)$""")

  def eachCompaction(logFile: LogFile, functionBlock: (String, Date, Long) => Unit)

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
