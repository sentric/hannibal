node "dev.hbase-0-90" {
  class { "apache::hbase::standalone":
    version => "0.90.6"
  }

  class { "hannibal":
    hbaseVersion => "0.90"
  }
}
