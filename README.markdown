![Hannibal][Hannibal-Logo]

 [Hannibal-Logo]: https://github.com/sentric/hannibal/blob/master/public/images/hannibal-logo-large-white.png?raw=true

Hannibal is a tool to help monitor and maintain [HBase][]-Clusters that are configured for [manual splitting][].

 [HBase]: http://hbase.apache.org
 [manual splitting]: http://hbase.apache.org/book/important_configurations.html#disable.splitting

While HBase provides metrics to monitor overall cluster health via JMX or Ganglia, it lacks the ability to monitor
single regions in an easy way. This information is essential when your cluster is configured for manual splits,
especially when the data growth is not uniform.

This tool tries to fill that gap by answering the following questions:

 * How well are regions balanced over the cluster?
 * How well are the regions split for each table?
 * How do regions evolve over time?

## Requirements & Limitations

Java 6 JDK is required on the machine where this tool is built. 

You will also need a browser with [SVG][]-Support to display Hannibal's graphs.

  [SVG]: http://en.wikipedia.org/wiki/Scalable_Vector_Graphics

### HBase Compatibilty

Hannibal currently supports HBase versions 0.90 to 0.94. The Scala-sources are currently compiled with Apache HBase versions wherever possible, you can try to alter the version in project/Build.scala if you wish to.

## Quickstart

### Variant 1: From Binary Package

 1. Download and extract the package according to your HBase version. Grab the Download-URL for the correct HBase-Version from the [Release-Page][Release-Page] and extract it:

        $ wget <URL-TO-PACKAGE>
        $ tar -xf "hannibal-hbase*.tgz"
        $ cd hannibal
	
 2. Copy `conf/hbase-site.template.xml` to `conf/hbase-site.xml` and adjust it:

 		$ cp conf/hbase-site.template.xml conf/hbase-site.xml
 		$ vi conf/hbase-site.xml
 		
 3. Run the start script inside the root folder of the project:
 
        $ ./start

When the application has started, you can access the web UI at: <http://localhost:9000>

Please note that history data about regions is only collected while the application is running, it will need to run for some time until the region detail graphs fill up. 

For information about the usage, check out [the Usage page on our Wiki][Wiki-Usage].

 [Wiki-Usage]: https://github.com/sentric/hannibal/wiki/Usage
 [Release-Page]: https://github.com/sentric/hannibal/releases/latest 

### Variant 2: From Source

 1. Grab the sources from github: 
 
        $ git clone https://github.com/sentric/hannibal.git
        $ cd hannibal

 2. Set the Environmentvariable HANNIBAL_HBASE_VERSION according to your HBase version. For example for HBase 0.92 do:

        $ export HANNIBAL_HBASE_VERSION=0.92
    
    Other possible values are "0.90" or "0.94". Be sure to always have this environment-variable set before executing any of the scripts: `build`, `start` or `sbt`. 


 3. Copy `conf/hbase-site.template.xml` to `conf/hbase-site.xml` and adjust it.

 4. Build the project using the build script inside the root folder of the project:
 
        $ ./build

 5. Run the start script inside the root folder of the project:
 
        $ ./start

The first time you build, [`sbt`][sbt] will fetch all dependencies needed to build and run the the
application. It will take a few minutes to build.

 [sbt]: http://www.scala-sbt.org/

When the application has started, you can access the web UI at: <http://localhost:9000>

Please note that history data about regions is only collected while the application is running, it will need to run for
some time until the region detail graphs fill up. 

For information about the usage, check out [the Usage page on our Wiki][Wiki-Usage].

 [Wiki-Usage]: https://github.com/sentric/hannibal/wiki/Usage

There is also a tutorial video on [YouTube][yt], that shows how to install and use Hannibal for HBase 0.90.
[yt]: http://www.youtube.com/watch?v=gu0lGAf7JO8

## Hue Integration
There is also [Hue][hue] App to integrate Hannibal into Hue. It is available [here][hannibal-hue].

[hue]: http://gethue.com/
[hannibal-hue]: https://github.com/ymc/hannibal-hue/

## Hannibal Mobile
If you have an Android Device, you might be interested in the Mobile App. The App which can be downloaded from [Google Play][gp]. Further details can be found in the [Wiki][Wiki-Mobile].

[gp]: https://play.google.com/store/apps/details?id=ch.ymc.hannibal.mobile
[Wiki-Mobile]: https://github.com/sentric/hannibal/wiki/Mobile

## Authentication 
If you want to make Hannibal reachable from the Internet, it's recommended to put a web server in front of it that takes care of handling authentication. Instructions can be found [in the Wiki][auth]. 
 
[auth]: https://github.com/sentric/hannibal/wiki/Auth

## How to display compactions
HBase 0.90.x's API doesn't allow you to query information on running compactions directly, so what we do is to parse
the RegionServers' log files directly, which are available through the service interface. HBase 0.92 allows you to query
compactions directly, but we still collect compactions using the logfile-parsing technique, because this way we don't miss 
any short running compactions.
The downside is that this doesn't work out of the box for all HBase clusters because either, the path-pattern or the
date-pattern can differ from system to system. Another problem can be, that the compaction-information isn't logged at
all in your setup, because your LogLevel is set too high.

If you run into problems with the the compaction-metrics, you should check the following parameters in [conf/application.conf](blob/master/conf/application.conf).

### 1. logfile.path-pattern
The default of the `logfile.path-pattern` is 

    logfile.path-pattern = "(?i)\"/logs/(.*regionserver.*[.].*)\""
 
The defaults should work for most setups in distributed mode. For standalone mode you will need change the pattern to

	logfile.path-pattern = (?i)\"/logs/(.*master.*[.].*)\"
	
If you are still unsure about the correct path-pattern, you can get a hint for the correct pattern by looking at your 
log-listing ```http://<<some-regionserver>>:60030/logs/```.

### 2. logfile.date-format
The default `logfile.date-format` is 

    logfile.date-format = "yyyy-MM-dd HH:mm:ss,SSS"

You can figure out the correct date-format by looking inside a logfile within your log-listing at ```http://<<some-regionserver>>:60030/logs/```

### 3. logfile.set-loglevels-on-startup
Informations about compactions are logged by HBase with `INFO`-Level, so the log-level for your HBase-Regionservers need to be set at least to `INFO`.

Hannibal can set the log level to `INFO` for you, just edit [conf/application.conf][]
and set

    logfile.set-loglevels-on-startup = true
If this doesn't work for you, you should try to manually change the loglevel on your regionservers.


## Tuning for large clusters
Hannibal is not yet ready to be used on large clusters (say about more than 100 machines), however some work has been done (thanks to [Alexandre Normand][] and [churrodog][]) to make it at least possible to run it without crashing on mid-sized clusters. If Hannibal's performance is not sufficient with your HBase setup, it may help to tune the following parameters in [conf/application.conf][].

### metrics.logfile-fetch-interval
This defaults to 300 seconds and it defines how often logfiles are collected from the different region servers. This process is quite heavy and for big clusters you should consider either to increase the interval or to disable it altogether by setting it to `0`

	metrics.logfile-fetch-interval = 0 # Disables compaction-metrics

### metrics.regions-fetch-interval 
This defaults to 60 seconds and you usually want to keep the update-interval high, because this way the UI will present you the most recent values and also will the region-history graph. However if you have many regions and it takes long to update the region metrics you may set this one to a value up to about 1800 (= 30 Minutes).

	metrics.regions-fetch-interval = 600 # 10 Minutes

### metrics.clean-threshold and metrics.clean-interval
Old metrics are cleaned after one day by default and it makes sense since the region history graphs are presenting data up to one day. However if you have a very large number of regions and your H2 database just explodes you may consider to reduce them.


## Deployment
If you intend to run Hannibal on a different host from where you want to build it, then you can run

	./create_package

This script generates a tgz-package inside the target folder, which you can then deploy on your target server. The HBase version can be set with the HANNIBAL_HBASE_VERSION environment variable, as described in the quickstart section.

## More Information

More information about the tool can be found in the [Wiki][]

 [Wiki]: https://github.com/sentric/hannibal/wiki

## License

Hannibal is released under MIT License, see [LICENSE][] for details.

 [LICENSE]: https://github.com/sentric/hannibal/blob/master/LICENSE      
 
## Contact   

If you need additional features or help please get in touch with us. Subscribe to the [Hannibal][] mailing list or follow [Hannibal App][] on Twitter.

[Hannibal App]: https://twitter.com/Hannibal_App            
[Hannibal]: https://groups.google.com/forum/#!forum/hannibal-app

## Contributors

This tool was developed at [Sentric][] by [Nils Kübler][] and [Jiayong Ou][]. [Sentric][] has been acquired by [YMC AG][] in May 2013.

With help from:

 * [Jean-Pierre König][]
 * [Christian Gügi][]
 * [Vadim Kisselmann][]
 * Ben Taylor
 * Stephanie Höhn
 * [Alexandre Normand][]
 * [churrodog][]
                           
 [YMC AG]: http://www.ymc.ch/en/expansion-of-the-portfolio-ymc-repositions-itself
 [Sentric]: http://www.sentric.ch
 [Nils Kübler]: https://twitter.com/nkuebler
 [Jiayong Ou]: https://twitter.com/jiayongou
 [Jean-Pierre König]: https://twitter.com/jpkoenig
 [Christian Gügi]: https://twitter.com/chrisgugi
 [Vadim Kisselmann]: https://twitter.com/vkisselmann
 [Alexandre Normand]: https://github.com/alexandre-normand
 [churrodog]: https://github.com/churrodog

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/51d84bade798b5b08bd69a6704be9315 "githalytics.com")](http://githalytics.com/sentric/hannibal)

[conf/application.conf]: blob/master/conf/application.conf

