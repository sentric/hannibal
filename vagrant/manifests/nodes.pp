node "hbase090" {
  class { "apache::hbase::standalone":
    version => "0.90.6"
  }

  class { "hannibal":
    hbaseVersion => "0.90"
  }
}

node "hbase092" {
  class { "apache::hbase::standalone":
    version => "0.92.2"
  }

  class { "hannibal":
    hbaseVersion => "0.92"
  }
}

node "hbase094" {
  class { "apache::hbase::standalone":
    version => "0.94.6.1"
  }

  class { "hannibal":
    hbaseVersion => "0.94"
  }
}
