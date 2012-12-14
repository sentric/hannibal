class hannibal::hbase_testdata_generator {

  service { "hbase-testdata-generator":
    ensure => "running",
    require => File["/etc/init.d/hbase-testdata-generator"]
  }

  file { "/etc/init.d/hbase-testdata-generator":
    ensure => "file",
    mode => 0777,
    source => "puppet:///modules/hannibal/etc/init.d/hbase-testdata-generator",
    require => File["/opt/hbase-testdata-generator"]
  }

  file { "/opt/hbase-testdata-generator":
    ensure => directory,
    recurse => true,
    source => "puppet:///modules/hannibal/opt/hbase-testdata-generator",
    require => Service["hadoop-hbase-master"]
  }

}