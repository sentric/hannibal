package models.hbase

/**
 * Created by nkuebler on 20/05/14.
 */
trait RegionLoad {
  val name:Array[Byte];
  val storeFiles:Int;
  val stores:Int;
  val storeFileSizeMB:Float;
  val memStoreSizeMB:Float;
}
