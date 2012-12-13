
class apache::hbase::standalone {
    require apache::hbase::base

    service { "hadoop-hbase-master":
        name => "apache-hbase-master",
        ensure => running,
        require => File["/etc/init.d/apache-hbase-master"]
    }

    file { "/etc/init.d/apache-hbase-master":
        ensure => file,
        source => "puppet:///modules/apache/etc/init.d/apache-hbase-master",
        mode => 0777
    }
}

class apache::hbase::base {
    require java

    apache::package { "hbase":
        version => "0.92.2",
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

