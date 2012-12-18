node "dev.hbase-0-90" {
  class { "apache::hbase::standalone":
    version => "0.90.6"
  }

  class { "hannibal":
    hbaseVersion => "0.90"
  }
}

node "dev.hbase-0-92" {
  class { "apache::hbase::standalone":
    version => "0.92.2"
  }

  class { "hannibal":
    hbaseVersion => "0.92"
  }
}
