class cloudera {

}

class cloudera::hbase::standalone {
    require cloduera::base

    service { "hadoop-hbase-master":
        ensure => running,
        require => Package["hadoop-hbase-master"],
    }

    package { "hadoop-hbase-master":
        ensure => latest,
    }
}

class cloduera::base {
    require cloudera::repository,java
}

class cloudera::repository {

    apt::source { "cloudera-repository":
        location => "http://archive.cloudera.com/debian",
        release => "lucid-cdh3u4",
        repos => "contrib",
        key => "02A818DD",
        key_source => "http://archive.cloudera.com/debian/archive.key"
    }

}