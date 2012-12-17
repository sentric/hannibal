/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import org.apache.hadoop.hbase.HServerInfo
import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.HServerLoad.RegionLoad

class RegionServer(val serverInfo: HServerInfo) {

  def infoUrl(url: String) =
    url
      .replaceAll("%hostname%", serverInfo.getHostname())
      .replaceAll("%infoport%", serverInfo.getInfoPort().toString)
      .replaceAll("%hostname-without-domain%", serverInfo.getHostname().split("\\.")(0))

  def serverName = {
    serverInfo.getServerName
  }

  def hostName = {
    serverInfo.getHostname
  }

  def port = {
    serverInfo.getServerAddress().getPort
  }

  def infoPort = {
    serverInfo.getInfoPort
  }

  def load = {
    serverInfo.getLoad
  }

  def regionsLoad:Iterable[RegionLoad] = {
    load.getRegionsLoad
  }

  override def toString = serverName

  override def equals(that:Any) = {
    that match {
      case other: RegionServer => other.serverInfo == serverInfo
      case _ => false
    }
  }
}
