/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package models

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HRegionInfo, TableName}

import scala.collection.mutable.ListBuffer
import play.api.Logger

import scala.collection._
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.libs.json.Json._
import models.hbase.{RegionLoad, RegionServer}
import globals.hBaseContext
import play.api.cache.Cache
import play.api.Play.current


case class Region(val regionServer: RegionServer, val regionLoad: RegionLoad) {

  val regionName        = Bytes.toStringBinary(regionLoad.name)

  val parsedRegionName  = RegionName(regionName)

  val serverName        = regionServer.serverName
  val serverHostName    = regionServer.hostName
  val serverPort        = regionServer.port
  val serverInfoPort    = regionServer.infoPort

  val storefiles        = regionLoad.storeFiles
  val stores            = regionLoad.stores
  val storefileSizeMB   = regionLoad.storeFileSizeMB
  val memstoreSizeMB    = regionLoad.memStoreSizeMB

  val parsedElements    = HRegionInfo.parseRegionName(regionLoad.name)

  val tableName         = parsedRegionName.tableName
  val startKey          = parsedRegionName.startKey
  val regionIdTimestamp = parsedRegionName.regionIdTimestamp

  // Kind ot regionName, without the startKey, to avoid strange routing issues.
  // This might be safely used within URIs
  val regionURI         = tableName + ",," + regionIdTimestamp + "." +
                            parsedRegionName.encodedName

  lazy val serverInfoUrl = "http://" + serverHostName + ":" + serverInfoPort

  lazy val info: RegionInfo = {
    val hRegionInfo =
      hBaseContext.hBase
        //.withAdmin { _.getConnection.getRegionLocation(Bytes.toBytes(tableName), parsedElements(1), false)}
          .withAdmin{_.getConnection.getRegionLocator(TableName.valueOf(tableName)).getRegionLocation(parsedElements(1), false)}
        .getRegionInfo
    RegionInfo(hRegionInfo)
  }
}

object Region {
  def all(): Seq[Region] =
    Cache.getAs[Seq[Region]]("regions.allRegions") getOrElse {
      Logger.warn("Region Cache not yet ready, forcing refresh!")
      refresh()
      Cache.getAs[Seq[Region]]("regions.allRegions") get
    }

  def findByName(regionName: String): Option[Region] =
    all().find((region) => RegionName(region.regionName) == RegionName(regionName))

  def forTable(tableName: String): Seq[Region] = {
    Cache.getAs[Map[String, Seq[Region]]]("regions.forTable") getOrElse {
      Logger.warn("Region Cache not yet ready, forcing refresh!")
      refresh()
      Cache.getAs[Map[String, Seq[Region]]]("regions.forTable") get
    } getOrElse (tableName, {
      Logger.error("Table '%s' not found".format(tableName))
      Seq[Region]()
    })
  }

  def refresh() = {
    val allRegions = hBaseContext.hBase.eachRegionServer { regionServer =>
      regionServer.regionsLoad.map(Region(regionServer, _))
    } flatten
    val groupedRegions = allRegions.groupBy(_.tableName)
    Cache.set("regions.allRegions", allRegions);
    Cache.set("regions.forTable", groupedRegions);
  }

  implicit val regionWrites = new Writes[Region] {
    def writes(region: Region) = Json.obj(
      "regionName" -> region.regionName,
      "serverName" -> region.serverName,
      "serverHostName" -> region.serverHostName,
      "serverPort" -> region.serverPort,
      "serverInfoPort" -> region.serverInfoPort,
      "storefiles" -> region.storefiles,
      "stores" -> region.stores,
      "storefileSizeMB" -> region.storefileSizeMB,
      "memstoreSizeMB" -> region.memstoreSizeMB,
      "tableName" -> region.tableName,
      "startKey" -> region.startKey,
      "regionIdTimestamp" -> region.regionIdTimestamp,
      "regionURI" -> region.regionURI,
      "serverInfoUrl" -> region.serverInfoUrl
    )
  }
}

case class RegionInfo(wrapped:HRegionInfo) {
  def endKey() = Bytes.toStringBinary(wrapped.getEndKey)
  def startKey() = Bytes.toStringBinary(wrapped.getStartKey)
  //def version() = wrapped.getVersion
  def regionId() = wrapped.getRegionId
  def regionName() = wrapped.getRegionNameAsString
}


case class RegionName(tableName: String, startKey: String, regionIdTimestamp: String, encodedName: String) {
  override def equals(that: Any): Boolean =
    that match {
      case r: RegionName => r.encodedName == this.encodedName &&
                              r.tableName == this.tableName &&
                              r.regionIdTimestamp == this.regionIdTimestamp
      case _ => false
    }
}

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
    val regionIdTimestampAndEncodedName = commaParts.last.split("\\.").view(0, 2)

    RegionName(
      tableName = commaParts.head,
      // startKey can contain commas itself, so we join all components except the first and last with a ","
      startKey = commaParts.view(1, commaParts.length - 1).reduceLeft(_ + "," + _),
      regionIdTimestamp = regionIdTimestampAndEncodedName.head.toString,
      encodedName = regionIdTimestampAndEncodedName.last
    )
  }

  implicit val regionNameWrites: Writes[RegionName] = new Writes[RegionName] {
     def writes(rn: RegionName) =
      toJson(JsObject(Seq(
        "tableName" -> toJson(rn.tableName),
        "startKey" -> toJson(rn.startKey),
        "regionIdTimestamp" -> toJson(rn.regionIdTimestamp),
        "encodedName" -> toJson(rn.encodedName)
      )))
  }
}
