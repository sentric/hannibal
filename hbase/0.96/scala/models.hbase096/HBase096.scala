/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models.hbase096

import scala.collection.JavaConverters._
import models.hbase.{HBase, RegionServer}

class HBase096 extends HBase {
  override def eachRegionServer[T](func: RegionServer => T) = {
    withAdmin { hbaseAdmin =>
      val status = hbaseAdmin.getClusterStatus()
      val servers = status.getServers().asScala.toList
      servers.map { serverName =>
        func(new RegionServer096(status, serverName, conf.getInt("hbase.regionserver.info.port", 60030)))
      }
    }
  }
}
