/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models.hbase

trait RegionServer {

  def infoUrl(url: String) =
    url
      .replaceAll("%hostname%", hostName)
      .replaceAll("%infoport%", infoPort.toString)
      .replaceAll("%hostname-without-domain%", hostName.split("\\.")(0))

  def serverName: String
  def hostName: String
  def port: Int
  def infoPort: Int
  def regionsLoad: Iterable[RegionLoad]

  override def toString = serverName
}
