node dev {

  # Use CDH Version ...
  include cloudera::hbase::standalone

  # ... or use Apache Version
  # include apache::hbase::standalone

  file { "/etc/init.d/hbase-testdata-generator":
    ensure => "link",
    target => "/vagrant/vagrant/files/hbase-testdata-generator-daemon.sh",
    require => Service["hadoop-hbase-master"]
  }

  service { "hbase-testdata-generator":
    ensure => "running",
    require => File["/etc/init.d/hbase-testdata-generator"]
  }

  include hannibal
}
