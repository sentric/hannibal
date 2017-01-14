package models.hbase120

import org.apache.hadoop.hbase.RegionLoad


/**
 * Created by Amit Anand on 09/22/2016.
 */
class RegionLoad120(val hbaseRegionLoad:RegionLoad) extends models.hbase.RegionLoad {
  override val memStoreSizeMB: Float = hbaseRegionLoad.getMemStoreSizeMB
  override val storeFileSizeMB: Float = hbaseRegionLoad.getStorefileSizeMB
  override val stores: Int = hbaseRegionLoad.getStores
  override val storeFiles: Int = hbaseRegionLoad.getStorefiles
  override val name = hbaseRegionLoad.getName
}
