package models.hbase096

import models.hbase.{HBase, HBaseContext}

/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
class HBaseContext096 extends HBaseContext {
  override val hBase:HBase = new HBase096
  override val logFileParser = new LogFileParser096
}
