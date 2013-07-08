/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models

import collection.mutable.ListBuffer
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.HTableDescriptor
import globals.hBaseContext

object Table {

  def all(): Seq[Table] = {
    val list = new ListBuffer[Table]()
    hBaseContext.hBase.eachTableDescriptor { desc =>
      list += Table(desc)
    }
    list.toList
  }

  def findByName(name: String): Table = {
    var desc:HTableDescriptor = null
    hBaseContext.hBase.withAdmin { admin =>
      desc = admin.getTableDescriptor(Bytes.toBytes(name))
    }
    if(desc != null)
       Table(desc)
    else
       null
  }

  def apply(wrapped: HTableDescriptor): Table = Table(
    name = Bytes.toString(wrapped.getName()),
    maxFileSize = wrapped.getMaxFileSize(),
    memstoreFlushSize = wrapped.getMemStoreFlushSize(),
    color = Palette.getColor(Bytes.toString(wrapped.getName())).toInt
  )
  
  def getTableColors(): Map[String, String] = {
    all().map {table => 
      val tableColor = Palette.getColor(table.name).toHtmlCode
      (table.name, tableColor)
    }.toMap
  }
}

case class Table(name:String, maxFileSize:Long, memstoreFlushSize:Long, color:Int)
