package models

import scala.collection.JavaConversions._
import hbase090.HBase090
import org.apache.hadoop.hbase.client.{HBaseAdmin, HTable}
import org.apache.hadoop.hbase.{HTableDescriptor, HBaseConfiguration}
import org.apache.hadoop.hbase.util.Bytes

/**
 * Abstract Factory to access HBase-API. Allows to separate parts that changed between HBase 0.90 and HBase 0.92.
 */
trait HBase {
  def eachRegionServer(functionBlock: (RegionServer) => Unit)

  val logFileParser:LogFileParser

  def withHTable(tableName:String, functionBlock: (HTable) => Unit) = {
    val conf = HBaseConfiguration.create()
    val table = new HTable(conf, Bytes.toBytes(tableName))
    try {
      functionBlock(table)
    } finally {
      table.close()
    }
  }

  def eachTableDescriptor(functionBlock: (HTableDescriptor) => Unit) = {
    withAdmin { admin =>
      admin.listTables().foreach { desc =>
        functionBlock(desc)
      }
    }
  }

  def withAdmin(functionBlock: (HBaseAdmin) => Unit) = {
    val conf = HBaseConfiguration.create()
    val client = new HBaseAdmin(conf)
    try {
      functionBlock(client)
    } finally {
      client.close()
    }
  }
}

object HBase extends HBase090