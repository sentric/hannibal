
class apache::hbase::standalone(
  $version = "0.90.6"
) {
    class { "apache::hbase::base":
      version => $version
    }

    service { "hadoop-hbase-master":
        name => "apache-hbase-master",
        ensure => running,
        require => File["/etc/init.d/apache-hbase-master"]
    }

    file { "/etc/init.d/apache-hbase-master":
        ensure => file,
        source => "puppet:///modules/apache/etc/init.d/apache-hbase-master",
        mode => 0777,
        require => Class["apache::hbase::base"]
    }
}

class apache::hbase::base(
  $version
) {
    require java

    apache::package { "hbase":
        version => $version,
        notify => Exec["permissions"]
    }

    group { "hbase":
        ensure => present,
        notify => Exec["permissions"]
    }

    user { "hbase":
        ensure => present,
        gid => "hbase",
        home => "/opt/hbase",
        notify => Exec["permissions"]
    }

    exec { "permissions":
        require => [User["hbase"], Group["hbase"], Apache::Package["hbase"]],
        refreshonly => true,
        command => "/bin/chown -R hbase:hbase /opt/hbase/"
    }

    file { "/var/log/hbase":
        require => Apache::Package["hbase"],
        ensure => "link",
        target => "/opt/hbase/logs"
    }

    file { "/usr/bin/hbase":
        require => Apache::Package["hbase"],
        ensure => file,
        source => "puppet:///modules/apache/usr/bin/hbase",
        mode => 0777
    }
}

