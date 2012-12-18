class hannibal(
  $hbaseVersion = "0.90"
) {
  require hannibal::hbase_testdata_generator

  file { "/etc/profile.d/hannibal.sh":
    ensure => file,
    mode => 0777,
    content => template("${module_name}/etc/profile.d/hannibal.sh")
  }
}