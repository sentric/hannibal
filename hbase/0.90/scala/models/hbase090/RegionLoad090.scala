package models.hbase090

import org.apache.hadoop.hbase.HServerLoad.RegionLoad

/**
 * Created by nkuebler on 20/05/14.
 */
class RegionLoad090(val hbaseRegionLoad:RegionLoad) extends models.hbase.RegionLoad {
  override val memStoreSizeMB: Float = hbaseRegionLoad.getMemStoreSizeMB
  override val storeFileSizeMB: Float = hbaseRegionLoad.getStorefileSizeMB
  override val stores: Int = hbaseRegionLoad.getStores
  override val storeFiles: Int = hbaseRegionLoad.getStorefiles
  override val name = hbaseRegionLoad.getName
}

