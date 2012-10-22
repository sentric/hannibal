/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package utils

import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.{HTableDescriptor, HServerInfo, HBaseConfiguration}
import org.apache.hadoop.hbase.client.{HTable, HBaseAdmin}
import org.apache.hadoop.hbase.util.Bytes

trait HBaseConnection {

  protected def withHTable(tableName:String, functionBlock: (HTable) => Unit) = {
    val conf = HBaseConfiguration.create()
    val table = new HTable(Bytes.toBytes(tableName))
    try {
      functionBlock(table)
    } finally {
      table.close()
    }
  }
  protected def eachTableDescriptor(functionBlock: (HTableDescriptor) => Unit) = {
    withHBaseAdmin { admin =>
      admin.listTables().foreach { desc =>
        functionBlock(desc)
      }
    }
  }

  protected def eachServerInfo(functionBlock: (HServerInfo) => Unit) = {
    withHBaseAdmin { hbaseAdmin =>
      val status = hbaseAdmin.getClusterStatus()
      val serverInfos = status.getServerInfo()
      serverInfos.foreach { serverInfo =>
        functionBlock(serverInfo)
      }
    }
  }

  protected def withHBaseAdmin(functionBlock: (HBaseAdmin) => Unit) = {
    val conf = HBaseConfiguration.create()
    val client = new HBaseAdmin(conf)
    try {
      functionBlock(client)
    } finally {
      client.close()
    }
  }
}

// Selfless Trait Pattern (http://www.artima.com/scalazine/articles/selfless_trait_pattern.html)
object HBaseConnection extends HBaseConnection