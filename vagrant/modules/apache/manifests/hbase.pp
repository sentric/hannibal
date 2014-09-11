
class apache::hbase::standalone(
  $archiveUrl = "http://archive.apache.org/dist/hbase/hbase-0.96.2/hbase-0.96.2-hadoop1-bin.tar.gz",

  $ipAddress = "127.0.0.1",

  $hostName = "localhost"
) {
    class { "apache::hbase::base":
      archiveUrl => $archiveUrl
    }

    file { "/etc/hosts":
      ensure => file,
      content => template("${module_name}/hosts.erb")
    }

    service { "hadoop-hbase-master":
        name => "apache-hbase-master",
        ensure => running,
        enable    => true,
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
  $archiveUrl
) {
    require java
#    package { "openjdk-6-jre":
#       ensure => "installed"
#    }

    apache::package { "hbase":
#        require => Package["openjdk-6-jre"],
        archiveUrl => $archiveUrl,
        target => "/opt/hbase",
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

