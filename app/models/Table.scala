/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package models

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.HTableDescriptor
import scala.util.control.Exception._
import scala.collection.immutable._
import globals.hBaseContext

object Table {
  def all(): Seq[Table] =
    hBaseContext.hBase.eachTableDescriptor { desc => Table(desc)}

  def findByName(name: String): Option[Table] =
    allCatch opt {
      hBaseContext.hBase.withAdmin(_.getTableDescriptor(Bytes.toBytes(name)))
    } map Table.apply

  def apply(wrapped: HTableDescriptor): Table = Table(
    name = Bytes.toString(wrapped.getName),
    maxFileSize = wrapped.getMaxFileSize,
    memstoreFlushSize = wrapped.getMemStoreFlushSize,
    color = Palette.getColor(Bytes.toString(wrapped.getName)).toInt
  )
  
  def getTableColors(): Map[String, String] = {
    all().map {table => 
      val tableColor = Palette.getColor(table.name).toHtmlCode
      (table.name, tableColor)
    }.toMap
  }
}

case class Table(name: String, maxFileSize: Long, memstoreFlushSize: Long, color: Int)
