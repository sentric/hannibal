/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import org.apache.hadoop.hbase.HServerInfo

class RegionServer(val serverInfo: HServerInfo) {

  def infoUrl(url: String) =
    url
      .replaceAll("%hostname%", serverInfo.getHostname())
      .replaceAll("%infoport%", serverInfo.getInfoPort().toString)
      .replaceAll("%hostname-without-domain%", serverInfo.getHostname().split("\\.")(0))

  def serverName = {
    serverInfo.getServerName
  }

  override def toString = serverName

  override def equals(that:Any) = {
    that match {
      case other: RegionServer => other.serverInfo == serverInfo
      case _ => false
    }
  }
}
