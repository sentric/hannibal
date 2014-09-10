node "hbase090" {
  class { "apache::hbase::standalone":
    archiveUrl => "http://archive.apache.org/dist/hbase/hbase-0.90.6/hbase-0.90.6.tar.gz",
    ipAddress => "192.168.80.90"
  }

  class { "hannibal":
    hbaseVersion => "0.90"
  }
}

node "hbase092" {
  class { "apache::hbase::standalone":
    archiveUrl => "http://archive.apache.org/dist/hbase/hbase-0.92.2/hbase-0.92.2.tar.gz",
    ipAddress => "192.168.80.92"
  }

  class { "hannibal":
    hbaseVersion => "0.92"
  }
}

node "hbase094" {
  class { "apache::hbase::standalone":
    archiveUrl => "http://archive.apache.org/dist/hbase/hbase-0.94.6.1/hbase-0.94.6.1.tar.gz",
    ipAddress => "192.168.80.94"
  }

  class { "hannibal":
    hbaseVersion => "0.94"
  }
}

node "hbase096" {
  class { "apache::hbase::standalone":
    archiveUrl => "http://archive.apache.org/dist/hbase/hbase-0.96.2/hbase-0.96.2-hadoop1-bin.tar.gz",
    ipAddress => "192.168.80.96"
  }

  class { "hannibal":
    hbaseVersion => "0.96"
  }
}

node "hbase098" {
  class { "apache::hbase::standalone":
    archiveUrl => "http://archive.apache.org/dist/hbase/hbase-0.98.5/hbase-0.98.5-hadoop1-bin.tar.gz",
    ipAddress => "192.168.80.98"
  }

  class { "hannibal":
    hbaseVersion => "0.98"
  }
}