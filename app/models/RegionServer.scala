/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import org.apache.hadoop.hbase.HServerInfo
import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.HServerLoad.RegionLoad
import utils.HBaseConnection

/**
 * Encapsulates accessing HBase-API regarding RegionServer information. Between HBase 0.90 and HBase 0.92 many
 * changes have been made regarding the RegionServer API. This class encapsulates this changes to provide
 * one single place to keep the rest of the code HBase-Verion independent.
 *
 * @param serverInfo HBase 0.90 HServerInfo object
 */
class RegionServer(val serverInfo: HServerInfo) {

  def infoUrl(url: String) =
    url
      .replaceAll("%hostname%", hostName)
      .replaceAll("%infoport%", infoPort.toString)
      .replaceAll("%hostname-without-domain%", hostName.split("\\.")(0))

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

object RegionServer extends HBaseConnection {
  def each(functionBlock: (RegionServer) => Unit) = {
    withHBaseAdmin { hbaseAdmin =>
      val status = hbaseAdmin.getClusterStatus()
      val serverInfos = status.getServerInfo()
      serverInfos.foreach { serverInfo =>
        functionBlock(new RegionServer(serverInfo))
      }
    }
  }
}
