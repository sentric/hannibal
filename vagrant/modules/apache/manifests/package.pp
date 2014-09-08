define apache::package(
    $target = "/opt/hbase",
    $archiveUrl = "",
) {
    $tmpArchive = "/tmp/hbase-download.tar.gz"

    exec { "download":
        command => "/usr/bin/wget ${archiveUrl} -O ${tmpArchive}",
        unless => "/usr/bin/stat ${$target}",
        notify => Exec["extract"]
    }

    file { "targetfolder" :
        name => $target,
        ensure => "directory"
    }

    exec { "extract":
        require => File["targetfolder"],
        command => "/bin/tar -C ${target} --strip-components=1 -xf ${tmpArchive}",
        refreshonly => true,
    }
}
