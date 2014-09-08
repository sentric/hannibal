package models.hbase090

import models.hbase.{HBase, HBaseContext}

/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
class HBaseContext090 extends HBaseContext {
  override val hBase:HBase = new HBase090
  override val logFileParser = new LogFileParser090
}
