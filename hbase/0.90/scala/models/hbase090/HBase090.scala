/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models.hbase090

import scala.collection.JavaConversions._
import models.hbase.{HBase, RegionServer}

class HBase090 extends HBase {
  override def eachRegionServer(functionBlock: (RegionServer) => Unit) = {
    withAdmin { hbaseAdmin =>
      val status = hbaseAdmin.getClusterStatus()
      val serverInfos = status.getServerInfo()
      serverInfos.foreach { serverInfo =>
        functionBlock(new RegionServer090(serverInfo))
      }
    }
  }
}
