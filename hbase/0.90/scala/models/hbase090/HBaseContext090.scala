package models.hbase090

import models.hbase.{HBase, HBaseContext}

/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
class HBaseContext090 extends HBaseContext {
  override val hBase:HBase = new HBase090
  override val logFileParser = new LogFileParser090
}
