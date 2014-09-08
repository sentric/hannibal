package models.hbase092

import models.hbase.{HBase, HBaseContext}

/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
class HBaseContext092 extends HBaseContext {
  override val hBase:HBase = new HBase092
  override val logFileParser = new LogFileParser092
}
