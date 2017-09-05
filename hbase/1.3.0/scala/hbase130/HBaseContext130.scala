package models.hbase130

import models.hbase.{HBase, HBaseContext}

/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
class HBaseContext130 extends HBaseContext {
  override val hBase:HBase = new HBase130
  override val logFileParser = new LogFileParser130
}
