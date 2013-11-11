/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models.hbase

import org.apache.hadoop.hbase.client.{HBaseAdmin, HTable}
import org.apache.hadoop.hbase.{HTableDescriptor, HBaseConfiguration}
import org.apache.hadoop.hbase.util.Bytes

/**
 * Abstract Factory to access HBase-API. Allows to separate parts that changed between HBase 0.90 and HBase 0.92.
 *
 * The concrete implementation is found in hannibal/hbase/[version]/scala
 */
trait HBase {
  def eachRegionServer[T](func: RegionServer => T): List[T]

  def withHTable[T](tableName: String, func: HTable => T): T = {
    val conf = HBaseConfiguration.create()
    val table = new HTable(conf, Bytes.toBytes(tableName))
    try {
      func(table)
    } finally {
      table.close()
    }
  }

  def eachTableDescriptor[T](func: HTableDescriptor => T): List[T] = {
    withAdmin { admin =>
      admin.listTables().toList.map { desc =>
        func(desc)
      }
    }
  }

  def withAdmin[T](func: HBaseAdmin => T): T = {
    val conf = HBaseConfiguration.create()
    val client = new HBaseAdmin(conf)
    try {
      func(client)
    } finally {
      client.close()
    }
  }
}