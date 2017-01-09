package models.hbase120

import models.hbase.{HBase, HBaseContext}

/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
class HBaseContext120 extends HBaseContext {
  override val hBase:HBase = new HBase120
  override val logFileParser = new LogFileParser120
}
