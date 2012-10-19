#!/bin/sh
script=$0
if [ -h $script ]; then
   script=`readlink $script`
fi
dir=`dirname $script`

java -Xmx1024M -XX:MaxPermSize=512m -jar $dir/sbt-launch-0.11.3.jar "$@"
