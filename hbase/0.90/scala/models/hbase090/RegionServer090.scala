/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package models.hbase090

import org.apache.hadoop.hbase.HServerLoad.RegionLoad
import org.apache.hadoop.hbase.HServerInfo
import scala.collection.JavaConverters._
import models.hbase.RegionServer

class RegionServer090(val serverInfo: HServerInfo) extends RegionServer {
  override val serverName = serverInfo.getServerName
  override val hostName = serverInfo.getHostname
  override val port = serverInfo.getServerAddress.getPort
  override val infoPort = serverInfo.getInfoPort
  lazy val load = serverInfo.getLoad
  override lazy val regionsLoad = load.getRegionsLoad.asScala.map( new RegionLoad090(_) )

  override def equals(that:Any) = {
    that match {
      case other: RegionServer090 => other.serverInfo == serverInfo
      case _ => false
    }
  }
}
