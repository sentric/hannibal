/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models.hbase090

import org.apache.hadoop.hbase.HServerLoad.RegionLoad
import org.apache.hadoop.hbase.HServerInfo
import scala.collection.JavaConversions._

class RegionServer090(val serverInfo: HServerInfo) extends models.RegionServer {
  override def serverName = {
    serverInfo.getServerName
  }

  override def hostName = {
    serverInfo.getHostname
  }

  override def port = {
    serverInfo.getServerAddress().getPort
  }

  override def infoPort = {
    serverInfo.getInfoPort
  }

  override def load = {
    serverInfo.getLoad
  }

  override def regionsLoad:Iterable[RegionLoad] = {
    load.getRegionsLoad
  }

  override def toString = serverName

  override def equals(that:Any) = {
    that match {
      case other: RegionServer090 => other.serverInfo == serverInfo
      case _ => false
    }
  }
}
