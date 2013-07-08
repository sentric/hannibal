/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models.hbase092

import scala.collection.JavaConversions._
import models.hbase.{HBase, RegionServer}

class HBase092 extends HBase {
  override def eachRegionServer(functionBlock: (RegionServer) => Unit) = {
    withAdmin { hbaseAdmin =>
      val status = hbaseAdmin.getClusterStatus()
      val servers = status.getServers()
      servers.foreach { serverName =>
        functionBlock(new RegionServer092(status, serverName))
      }
    }
  }
}
