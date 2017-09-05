/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package models.hbase

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Admin, Table}
import org.apache.hadoop.hbase.{HBaseConfiguration, HTableDescriptor}
import org.apache.hadoop.security.UserGroupInformation
import org.apache.hadoop.hbase.TableName
import play.api.Logger

/**
 * Abstract Factory to access HBase-API. Allows to separate parts that changed between HBase 0.90 and HBase 0.92.
 *
 * The concrete implementation is found in hannibal/hbase/[version]/scala
 */
trait HBase {

  val conf:Configuration = HBaseConfiguration.create()
  val AUTH_METHOD = conf.get("hadoop.security.authentication", "SIMPLE")

  Logger.info("Authentication Method : " + AUTH_METHOD)

  if (AUTH_METHOD.toUpperCase == "KERBEROS") {

    val DEFAULT_KT_FILE = "/etc/security/keytabs/hannibal.service.keytab"
    val DEFAULT_KT_USER = "hannibal"
    val KT_FILE_KEY = "hannibal.kerberos.keytab"
    val KT_USER_KEY = "hannibal.kerberos.principal"

    val HANNIBAL_KT_FILE = conf.get(KT_FILE_KEY, DEFAULT_KT_FILE)
    val HANNIVAL_KT_USER = conf.get(KT_USER_KEY, DEFAULT_KT_USER)

    Logger.info("Hannibal keytab : " + HANNIBAL_KT_FILE)
    Logger.info("Hannibal User : " + HANNIVAL_KT_USER)

    UserGroupInformation.setConfiguration(conf)

    if(new java.io.File(HANNIBAL_KT_FILE).exists())
      UserGroupInformation.loginUserFromKeytab(HANNIVAL_KT_USER, HANNIBAL_KT_FILE)
  }

  val conn:Connection = ConnectionFactory.createConnection(conf)

  def eachRegionServer[T](func: RegionServer => T): List[T]

  def withHTable[T](tableName: String, func: Table => T): T = {
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
}
