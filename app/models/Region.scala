/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import play.Logger
import scala.collection.mutable.ListBuffer
import org.apache.hadoop.hbase.client.HBaseAdmin
import utils.HBaseConnection
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HRegionInfo, HRegionLocation, HServerLoad}
import play.api.libs.concurrent.Promise
import play.api.libs.concurrent.Akka
import play.api.Play.current

case class Region(serverName       : String,
                      serverHostName   : String,
                      serverPort       : Int,
                      serverInfoPort   : Int,
                      
                      regionName       : String,
                      storefiles       : Int,
                      stores           : Int,
                      storefileSizeMB  : Int,
                      memstoreSizeMB   : Int,
                      
                      tableName        : String,
                      startKey         : String,
                      regionIdTimestamp: Long)
extends HBaseConnection {

  def getRegionInfo() = {
    var loc:HRegionLocation = null;
    withHBaseAdmin { admin =>
      val connection = admin.getConnection()
      loc = connection.getRegionLocation(Bytes.toBytes(tableName), Bytes.toBytes(startKey), false)
    }
    RegionInfo(loc.getRegionInfo())
  }

  def serverInfoUrl() = "http://" + serverHostName + ":" + serverInfoPort

  def findLongestCompactionInLastWeek() = {
    val metric = MetricDef.COMPACTIONS(regionName).metric(MetricDef.now()-1000*3600*24*7, MetricDef.now())
    var begin = metric.begin;
    var max = 0L;
    metric.values.foreach { record =>
      if(record.v > 0)
        begin = record.ts
      else
        max = scala.math.max(max, record.ts - begin)
    }
    max
  }
}

object Region {
  def apply(regionServer: RegionServer, regionLoad: HServerLoad.RegionLoad) : Region = {
    val regionName = regionLoad.getNameAsString()
    val parsedRegionName = RegionName(regionName)

    Region(
               serverName        = regionServer.serverName,
               serverHostName    = regionServer.hostName,
               serverPort        = regionServer.port,
               serverInfoPort    = regionServer.infoPort,
               
               regionName        = regionName,
               stores            = regionLoad.getStores(),
               storefiles        = regionLoad.getStorefiles(),
               storefileSizeMB   = regionLoad.getStorefileSizeMB(),
               memstoreSizeMB    = regionLoad.getMemStoreSizeMB(),
               
               tableName         = parsedRegionName.tableName,
               startKey          = parsedRegionName.startKey,
               regionIdTimestamp = parsedRegionName.regionIdTimestamp)
  }

  def allAsync(): Promise[Seq[Region]] = {
    Akka.future { all() }
  }
  def all(): Seq[Region] = {
    val list = new ListBuffer[Region]()

    HBase.eachRegionServer { regionServer =>
      regionServer.regionsLoad.foreach { regionLoad =>
        list += Region(regionServer, regionLoad)
      }
    }

    list.toList
  }

  def findByNameAsync(regionName: String): Promise[Region] = {
    allAsync().map { infos =>
      infos.find { ri =>
        ri.regionName == regionName
      }.getOrElse(null)
    }
  }
  def findByName(regionName: String): Region = findByNameAsync(regionName).value.get

  def forTableAsync(tableName: String): Promise[Seq[Region]] = {
    allAsync().map{ regions => regions.filter(_.tableName == tableName) }
  }
  def forTable(tableName: String): Seq[Region] = forTableAsync(tableName).value.get
}


case class RegionInfo(wrapped:HRegionInfo) {
  def endKey() = Bytes.toString(wrapped.getEndKey())
  def startKey() = Bytes.toString(wrapped.getStartKey())
  def version() = wrapped.getVersion()
  def regionId() = wrapped.getRegionId()
  def regionName() = wrapped.getRegionNameAsString()
}


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