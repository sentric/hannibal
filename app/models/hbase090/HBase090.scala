package models.hbase090

import scala.collection.JavaConversions._

class HBase090 extends models.HBase {
  override def eachRegionServer(functionBlock: (models.RegionServer) => Unit) = {
    withAdmin { hbaseAdmin =>
      val status = hbaseAdmin.getClusterStatus()
      val serverInfos = status.getServerInfo()
      serverInfos.foreach { serverInfo =>
        functionBlock(new RegionServer090(serverInfo))
      }
    }
  }

  override val logFileParser = new LogFileParser090
}
