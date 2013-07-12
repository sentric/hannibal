/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models

import play.Logger
import scala.collection.mutable.ListBuffer
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HRegionInfo, HRegionLocation, HServerLoad}
import play.api.libs.concurrent.Promise
import play.api.libs.concurrent.Akka
import play.api.Play.current
import org.codehaus.jackson.annotate.{JsonIgnoreProperties, JsonIgnore}
import play.api.libs.json.{JsObject, Writes}
import play.api.libs.json.Json._
import play.api.mvc._
import globals.hBaseContext
import models.hbase.RegionServer

@JsonIgnoreProperties(Array("parsedRegionName", "regionServer", "regionLoad"))
case class Region(val regionServer:RegionServer,  val regionLoad:HServerLoad.RegionLoad) {

  val regionName = Bytes.toStringBinary(regionLoad.getName())

  val parsedRegionName = RegionName(regionName)

  val serverName        = regionServer.serverName
  val serverHostName    = regionServer.hostName
  val serverPort        = regionServer.port
  val serverInfoPort    = regionServer.infoPort

  val storefiles        = regionLoad.getStorefiles()
  val stores            = regionLoad.getStores()
  val storefileSizeMB   = regionLoad.getStorefileSizeMB()
  val memstoreSizeMB    = regionLoad.getMemStoreSizeMB()

  val parsedElements    = HRegionInfo.parseRegionName(regionLoad.getName())

  val tableName         = parsedRegionName.tableName
  val startKey          = parsedRegionName.startKey
  val regionIdTimestamp = parsedRegionName.regionIdTimestamp

  // Kind ot regionName, without the startKey, to avoid strange routing issues.
  // This might be safely used within URIs
  val regionURI         = tableName + ",," + regionIdTimestamp + "." +
                            parsedRegionName.encodedName

  def getRegionInfo() = {
    var loc:HRegionLocation = null;
    hBaseContext.hBase.withAdmin { admin =>
      val connection = admin.getConnection()
      loc = connection.getRegionLocation(Bytes.toBytes(tableName), parsedElements(1), false)
    }
    RegionInfo(loc.getRegionInfo())
  }

  def serverInfoUrl() = "http://" + serverHostName + ":" + serverInfoPort
}

object Region {

  def allAsync(): Promise[Seq[Region]] = {
    Akka.future { all() }
  }
  def all(): Seq[Region] = {
    val list = new ListBuffer[Region]()

    hBaseContext.hBase.eachRegionServer { regionServer =>
      regionServer.regionsLoad.foreach { regionLoad =>
        list += Region(regionServer, regionLoad)
      }
    }

    list.toList
  }

  def findByNameAsync(regionName: String): Promise[Region] = {
    allAsync().map { infos =>
      infos.find { ri =>
        RegionName(ri.regionName) == RegionName(regionName)
      }.getOrElse(null)
    }

  }

  def findByName(regionName: String): Region = {
    findByNameAsync(regionName).value.get
  }

  def forTableAsync(tableName: String): Promise[Seq[Region]] = {
    allAsync().map{ regions => regions.filter(_.tableName == tableName) }
  }
  def forTable(tableName: String): Seq[Region] = forTableAsync(tableName).value.get
}


case class RegionInfo(wrapped:HRegionInfo) {
  def endKey() = Bytes.toStringBinary(wrapped.getEndKey())
  def startKey() = Bytes.toStringBinary(wrapped.getStartKey())
  def version() = wrapped.getVersion()
  def regionId() = wrapped.getRegionId()
  def regionName() = wrapped.getRegionNameAsString()
}


case class RegionName(tableName: String, startKey: String, regionIdTimestamp: Long, encodedName: String) {

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
      regionIdTimestamp = regionIdTimestampAndEncodedName.head.toLong,
      encodedName = regionIdTimestampAndEncodedName.last
    )
  }

  implicit def regionNameWrites : Writes[RegionName] = new Writes[RegionName] {
     def writes(rn: RegionName) = {
      toJson(JsObject(Seq(
        "tableName" -> toJson(rn.tableName),
        "startKey" -> toJson(rn.startKey),
        "regionIdTimestamp" -> toJson(rn.regionIdTimestamp),
        "encodedName" -> toJson(rn.encodedName)
      )))
    }
  }
}