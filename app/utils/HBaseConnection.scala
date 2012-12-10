/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package utils

import play.Logger
import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.{HTableDescriptor, HServerLoad, HBaseConfiguration, ServerName, ClusterStatus}
import org.apache.hadoop.hbase.client.{HTable, HBaseAdmin}
import org.apache.hadoop.hbase.util.Bytes

trait HBaseConnection {

  protected def withHTable(tableName:String, functionBlock: (HTable) => Unit) = {
    val conf = HBaseConfiguration.create()
    val table = new HTable(conf, Bytes.toBytes(tableName))
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

  protected def eachServer(functionBlock: (HBaseAdmin, ClusterStatus, ServerName) => Unit) = {
    withHBaseAdmin { hbaseAdmin =>
      val clusterStatus = hbaseAdmin.getClusterStatus()
      val servers = clusterStatus.getServers()
      servers.foreach { serverName =>
        functionBlock(hbaseAdmin, clusterStatus, serverName)
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