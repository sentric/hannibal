package models.hbase113

import models.hbase.{HBase, HBaseContext}

/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
class HBaseContext113 extends HBaseContext {
  override val hBase:HBase = new HBase113
  override val logFileParser = new LogFileParser113
}
