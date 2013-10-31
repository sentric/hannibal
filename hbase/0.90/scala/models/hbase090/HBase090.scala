/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models.hbase090

import scala.collection.JavaConversions._
import models.hbase.{HBase, RegionServer}

class HBase090 extends HBase {
  override def eachRegionServer[T](func: RegionServer => T) = {
    withAdmin { hbaseAdmin =>
      val status = hbaseAdmin.getClusterStatus()
      val serverInfos = status.getServerInfo()
      serverInfos.map { serverInfo =>
        func(new RegionServer090(serverInfo))
      }
    }
  }
}
