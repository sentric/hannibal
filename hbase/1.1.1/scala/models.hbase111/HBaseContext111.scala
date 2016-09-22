package models.hbase111

import models.hbase.{HBase, HBaseContext}

/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
class HBaseContext111 extends HBaseContext {
  override val hBase:HBase = new HBase111
  override val logFileParser = new LogFileParser111
}
