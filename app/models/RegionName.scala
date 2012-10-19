/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

case class RegionName(tableName: String, startKey: String, regionIdTimestamp: Long, encodedName: String)
object RegionName {
  /**
   * Region name in HBase is composed of "<tableName>,<startKey>,<regionIdTimestamp>.<encodedName>."
   */
  def apply(regionName: String) : RegionName = {
    // Parse out comma separated components
    val commaParts = regionName.split(",")
    
    // Last comma separated components contains <regionIdTimestamp>.<encodedName>."
    // We only need the first two dot splitted components sicne the last one is empty
    // (note the dot at the end...)
    val regionIdTimestampAndEncodedName = commaParts.last.split("\\.").view(0, 1)

    RegionName(
      tableName = commaParts.head, 
      // startKey can contain commas itself, so we join all components except the first and last with a ","
      startKey = commaParts.view(1, commaParts.length - 1).reduceLeft(_ + "," + _), 
      regionIdTimestamp = regionIdTimestampAndEncodedName.head.toLong,
      encodedName = regionIdTimestampAndEncodedName.last
    )
  }
}