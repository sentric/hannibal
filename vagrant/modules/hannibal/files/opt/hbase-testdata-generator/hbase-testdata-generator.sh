#!/bin/sh
script=$0
if [ -h $script ]; then
        script=`readlink $script`
fi
dir=`dirname $script`
abs_dir="`cd $dir; pwd`"

LOGFILE="/var/log/hbase-testdata-generator.log"

exec hbase shell $abs_dir/hbase-testdata-generator.rb > $LOGFILE 2>&1