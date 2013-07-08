define apache::package(
    $target = "/opt",
    $version = "",
    $symlink = true,
    $mirror = "http://archive.apache.org/",
) {
    $folder = "${name}-${version}"
    $archive = "${folder}.tar.gz"

    exec { "download":
        command => "/usr/bin/wget ${mirror}/dist/${name}/${folder}/${archive} -O /tmp/${archive}",
        unless => "/usr/bin/stat ${target}/${folder}",
        notify => Exec["extract"]
    }

    exec { "extract":
        command => "/bin/tar -C ${target} -xf /tmp/${archive}",
        refreshonly => true,
    }

    if ( $symlink ) {
        file { "${target}/${name}":
            ensure => symlink,
            target => "${target}/${folder}",
        }
    }
}
