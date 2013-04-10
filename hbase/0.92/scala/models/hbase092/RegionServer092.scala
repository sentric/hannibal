/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models.hbase092

import org.apache.hadoop.hbase.HServerLoad.RegionLoad
import org.apache.hadoop.hbase.{ServerName, ClusterStatus, HServerInfo}
import scala.collection.JavaConversions._

class RegionServer092(val clusterStatus:ClusterStatus, val serverNameObj:ServerName) extends models.RegionServer {
  override def serverName = {
    serverNameObj.getServerName()
  }

  override def hostName = {
    serverNameObj.getHostname()
  }

  override def port = {
    serverNameObj.getPort()
  }

  override def infoPort = {
    60030
  }

  override def load = {
    clusterStatus.getLoad(serverNameObj)
  }

  override def regionsLoad:Iterable[RegionLoad] = {
    load.getRegionsLoad().values
  }

  override def toString = serverName

  override def equals(that:Any) = {
    that match {
      case other: RegionServer092 => other.serverNameObj == serverNameObj
      case _ => false
    }
  }
}
