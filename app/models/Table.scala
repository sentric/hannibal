package models

import utils.HBaseConnection
import collection.mutable.ListBuffer
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.HTableDescriptor

/**
 * Created by IntelliJ IDEA.
 * User: nkuebler
 * Date: 08.10.12
 * Time: 17:56
 * To change this template use File | Settings | File Templates.
 */

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