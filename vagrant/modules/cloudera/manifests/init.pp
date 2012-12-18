class cloudera {

}

class cloudera::hbase::standalone {
    require cloduera::base

    service { "hadoop-hbase-master":
        name => "hadoop-hbase-master", # for 0.92 its: "hbase-master"
        ensure => running,
        require => Package["hadoop-hbase-master"],
    }

    package { "hadoop-hbase-master":
        name => "hadoop-hbase-master", # for 0.92 its: "hbase-master"
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
        # for 0.92 its:
        # location => "http://archive.cloudera.com/cdh4/ubuntu/lucid/amd64/cdh",
        # release => "lucid-cdh4.1.2",
        repos => "contrib",
        key => "02A818DD",
        key_source => "http://archive.cloudera.com/debian/archive.key"
        # for 0.92 its:
        # key_source => "http://archive.cloudera.com/cdh4/ubuntu/lucid/amd64/cdh/archive.key"
    }

}