/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package models.hbase130

import scala.collection.JavaConverters._
import models.hbase.{HBase, RegionServer}

class HBase130 extends HBase {
  override def eachRegionServer[T](func: RegionServer => T) = {
    withAdmin { hbaseAdmin =>
      val status = hbaseAdmin.getClusterStatus()
      val servers = status.getServers().asScala.toList
      servers.map { serverName =>
        func(new RegionServer130(status, serverName, conf.getInt("hbase.regionserver.info.port", 16030)))
      }
    }
  }


}
