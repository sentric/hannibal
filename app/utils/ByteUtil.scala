package utils

import org.apache.hadoop.hbase.util.Bytes

object ByteUtil
{
  def toHexString(b: Array[Byte]): String = {
    var string = "";
    for (i <- 0 until b.length) {
      string += Integer.toString((b(i) & 0xff) + 0x100, 16).substring(1)
    }
    string
  }
}