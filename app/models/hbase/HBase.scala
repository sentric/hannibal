/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models.hbase

import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.client.{HBaseAdmin, HTable}
import org.apache.hadoop.hbase.{HTableDescriptor, HBaseConfiguration}
import org.apache.hadoop.hbase.util.Bytes

/**
 * Abstract Factory to access HBase-API. Allows to separate parts that changed between HBase 0.90 and HBase 0.92.
 *
 * The concrete implementation is found in hannibal/hbase/[version]/scala
 */
trait HBase {
  def eachRegionServer(functionBlock: (RegionServer) => Unit)

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