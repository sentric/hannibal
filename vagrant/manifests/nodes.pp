node "hbase090" {
  class { "apache::hbase::standalone":
    archiveUrl => "http://archive.apache.org/dist/hbase/hbase-0.90.6/hbase-0.90.6.tar.gz"
  }

  class { "hannibal":
    hbaseVersion => "0.90"
  }
}

node "hbase092" {
  class { "apache::hbase::standalone":
    archiveUrl => "http://archive.apache.org/dist/hbase/hbase-0.92.2/hbase-0.92.2.tar.gz"
  }

  class { "hannibal":
    hbaseVersion => "0.92"
  }
}

node "hbase094" {
  class { "apache::hbase::standalone":
    archiveUrl => "http://archive.apache.org/dist/hbase/hbase-0.94.6.1/hbase-0.94.6.1.tar.gz"
  }

  class { "hannibal":
    hbaseVersion => "0.94"
  }
}

node "hbase096" {
  class { "apache::hbase::standalone":
    archiveUrl => "http://archive.apache.org/dist/hbase/hbase-0.96.2/hbase-0.96.2-hadoop1-bin.tar.gz"
  }

  class { "hannibal":
    hbaseVersion => "0.96"
  }
}
