node dev {

  # No CDH Version of HBase 0.94 available yet, so use Apache Version
  include apache::hbase::standalone

  include hannibal
}
