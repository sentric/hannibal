package models

import org.apache.hadoop.hbase.HServerInfo
import utils.HBaseConnection

object HBase extends HBaseConnection {
  def eachRegionServer(functionBlock: (RegionServer) => Unit) = {
    eachServerInfo { serverInfo =>
      functionBlock(new RegionServer(serverInfo))
    }
  }
}
