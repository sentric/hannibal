node dev {
  include cloudera::hbase::standalone

  file { "/etc/init.d/hbase-testdata-generator":
    ensure => "link",
    target => "/vagrant/vagrant/files/hbase-testdata-generator-daemon.sh",
    require => Service["hadoop-hbase-master"]
  }

  service { "hbase-testdata-generator":
    ensure => "running",
    require => File["/etc/init.d/hbase-testdata-generator"]
  }
}
