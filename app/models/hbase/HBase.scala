/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package models.hbase

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Admin, Table}
import org.apache.hadoop.hbase.{HBaseConfiguration, HTableDescriptor}
//import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.security.UserGroupInformation
import org.apache.hadoop.hbase.TableName


/**
 * Abstract Factory to access HBase-API. Allows to separate parts that changed between HBase 0.90 and HBase 0.92.
 *
 * The concrete implementation is found in hannibal/hbase/[version]/scala
 */
trait HBase {
  val conf:Configuration = HBaseConfiguration.create()
  val AUTH_METHOD = conf.get("hadoop.security.authentication", "SIMPLE")

  if (AUTH_METHOD == "KERBEROS")
    UserGroupInformation.setConfiguration(conf)

  val conn:Connection = ConnectionFactory.createConnection(conf)
  //val client:Admin = conn.getAdmin();

  def eachRegionServer[T](func: RegionServer => T): List[T]

  def withHTable[T](tableName: String, func: Table => T): T = {
    //val conf = HBaseConfiguration.create()
    //val table = new HTable(conf, Bytes.toBytes(tableName))
    val table:Table = conn.getTable(TableName.valueOf(tableName))
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

  def withAdmin[T](func: Admin => T): T = {
    val client = conn.getAdmin()
    try {
      func(client)
    } finally {
      client.close()
    }
  }

//  def withAdmin[T](func: HBaseAdmin => T): T = {
//    val client = new HBaseAdmin(conf)
//    try {
//      func(client)
//    } finally {
//      client.close()
//    }
//  }
}
