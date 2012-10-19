/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

import utils.HBaseConnection
import collection.mutable.ListBuffer
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.HTableDescriptor

object Table extends HBaseConnection {

  def all(): Seq[Table] = {
    val list = new ListBuffer[Table]()
    withTableDescriptors { desc =>
      list += Table(desc)
    }
    list.toList
  }

  def findByName(name: String): Table = {
    var desc:HTableDescriptor = null
    withHBaseAdmin { admin =>
      desc = admin.getTableDescriptor(Bytes.toBytes(name))
    }
    if(desc != null)
       Table(desc)
    else
       null
  }

  def apply(wrapped: HTableDescriptor): Table = Table(
    name = Bytes.toString(wrapped.getName()),
    maxFileSize = wrapped.getMaxFileSize()
  )
}

case class Table(name:String, maxFileSize:Long)