/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import play.Logger
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import org.apache.hadoop.hbase.client.HBaseAdmin
import utils.HBaseConnection
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HRegionInfo, HRegionLocation, ServerName, HServerLoad}
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

object Region extends HBaseConnection {
  def apply(serverName: ServerName, regionLoad: HServerLoad.RegionLoad) : Region = {
    val regionName = regionLoad.getNameAsString()
    val parsedRegionName = RegionName(regionName)

    Region(
               serverName        = serverName.getServerName(),
               serverHostName    = serverName.getHostname(),
               serverPort        = serverName.getPort(),
               serverInfoPort    = 60030,
               
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

    eachServer { (hbaseAdmin, clusterStatus, serverName) =>
      val load = clusterStatus.getLoad(serverName)
      val regionsLoad = load.getRegionsLoad();
      regionsLoad.values.foreach { (regionLoad) =>
        list += Region(serverName, regionLoad)
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