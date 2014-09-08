package models.hbase

/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
trait HBaseContext {
  val hBase: HBase
  val logFileParser: LogFileParser
}
