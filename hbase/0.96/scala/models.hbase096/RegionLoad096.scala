package models.hbase096

import org.apache.hadoop.hbase.RegionLoad


/**
 * Created by nkuebler on 20/05/14.
 */
class RegionLoad096(val hbaseRegionLoad:RegionLoad) extends models.hbase.RegionLoad {
  override val memStoreSizeMB: Float = hbaseRegionLoad.getMemStoreSizeMB
  override val storeFileSizeMB: Float = hbaseRegionLoad.getStorefileSizeMB
  override val stores: Int = hbaseRegionLoad.getStores
  override val storeFiles: Int = hbaseRegionLoad.getStorefiles
  override val name = hbaseRegionLoad.getName
}
