package models.hbase096

import models.hbase.{HBase, HBaseContext}

/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
class HBaseContext096 extends HBaseContext {
  override val hBase:HBase = new HBase096
  override val logFileParser = new LogFileParser096
}
