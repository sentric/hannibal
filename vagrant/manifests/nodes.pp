node dev {

  # Use CDH Version ...
  include cloudera::hbase::standalone

  # ... or use Apache Version
  # include apache::hbase::standalone

  include hannibal
}
