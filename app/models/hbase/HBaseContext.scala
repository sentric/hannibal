package models.hbase

/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
trait HBaseContext {
    val hBase:HBase
    val logFileParser:LogFileParser
}
