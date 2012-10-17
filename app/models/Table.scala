package models

import utils.HBaseConnection
import collection.mutable.ListBuffer
import org.apache.hadoop.hbase.util.Bytes

/**
 * Created by IntelliJ IDEA.
 * User: nkuebler
 * Date: 08.10.12
 * Time: 17:56
 * To change this template use File | Settings | File Templates.
 */

case class Table(name: String)

object Table extends HBaseConnection {

  def all(): Seq[Table] = {
    val list = new ListBuffer[Table]()
    withTableDescriptors { desc =>
      list += Table(Bytes.toString(desc.getName()))
    }
    list.toList
  }
}