![Hannibal][Hannibal-Logo]

 [Hannibal-Logo]: https://github.com/sentric/hannibal/blob/master/public/images/hannibal-logo-large-white.png?raw=true

Hannibal is a tool to help monitor and maintain [HBase][]-Clusters that are configured for
[manual splitting][].

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

### HBase Compatibilty

Hannibal currently supports HBase versions 0.90 to 0.94.  Be sure to checkout the correct branch for your version. Although we prefer Cloudera's versions, you can try to alter the version in project/Build.scala if you wish to.

- HBase 0.90.6-cdh3u4 support is available in the branch [master][master]
- HBase 0.92.1-cdh4.1.2 support is available in the branch [hbase-0.92][b92]
- HBase 0.94.1 support is available in the branch [habse-0.94][b94]

[master]: https://github.com/sentric/hannibal/
[b92]: https://github.com/sentric/hannibal/tree/hbase-0.92
[b94]: https://github.com/sentric/hannibal/tree/hbase-0.94

## Video Tutorial

There is also a tutorial video on [YouTube][yt], that shows how to install and use Hannibal for HBase 0.90.
[yt]: http://www.youtube.com/watch?v=I4Kto41a5kE&hd=1

## Quickstart

 1. Grab the sources from github: 
 
        $ git clone https://github.com/sentric/hannibal.git
        $ cd hannibal

 2. Change the branch according to your HBase version (for example for HBase-0.92 sdo):

        $ git checkout hbase-0.92
    
    see the [HBase Compatibilty][hc] Section for a complete list of supported HBase versions.
    
[hc]: https://github.com/sentric/hannibal/#hbase-compatibility

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

## How to display compactions

HBase 0.90.x's API doesn't allow you to query information on running compactions directly, so what we do is to parse
the RegionServers' log files directly, which are available through the service interface. HBase 0.92 allows to query
compactions directly, but we still collect compactions using the logfile-parsing way, because this way we don't miss 
any short running compactions.
The downside is that this doesn't work without further configuration because either, the url-pattern and the 
date pattern can differ from system to system. Regardless of which version of HBase you use, you should check those 
parameters in [conf/application.conf](blob/master/conf/application.conf):

    compactions.logfile-url-pattern = "..."
    compactions.logfile-date-format = "yyyy-MM-dd HH:mm:ss,SSS"
    
Informations about compactions are logged with `INFO`-Level, so the log levels need to be set at least to `INFO`.

Hannibal can set the log level to `INFO` for you, just edit [conf/application.conf](blob/master/conf/application.conf)
and set

    compactions.set-loglevels-on-startup = true
if you have problems, please make sure that the url-pattern is correct

    compactions.loglevel-url-pattern = "..." 

Please let [me][Nils Kübler] know if you have trouble with this, or have an idea how we could record information more
easily.

## More Information

More information about the tool can be found in the [Wiki][]

 [Wiki]: https://github.com/sentric/hannibal/wiki

## Additional Features

If you need additional features please get in [touch with us](http://sentric.ch/contact), maybe we can work something
out.

## License

Hannibal is released under MIT License, see [LICENSE][] for details.

 [LICENSE]: https://github.com/sentric/hannibal/blob/master/LICENSE

## Contributors

This tool is developed at [Sentric][] by: [Nils Kübler][] and [Jiayong Ou][]

With help from:

 * [Jean-Pierre König][]
 * [Christian Gügi][]
 * [Vadim Kisselmann][]
 * Ben Taylor
 * Stephanie Höhn
 * [Alexandre Normand][]

 [Sentric]: http://www.sentric.ch
 [Nils Kübler]: https://twitter.com/nkuebler
 [Jiayong Ou]: https://twitter.com/jiayongou
 [Jean-Pierre König]: https://twitter.com/jpkoenig
 [Christian Gügi]: https://twitter.com/chrisgugi
 [Vadim Kisselmann]: https://twitter.com/vkisselmann
 [Alexandre Normand]: https://github.com/alexandre-normand

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/ed9e66b101612798e3e015369f86b502 "githalytics.com")](http://githalytics.com/sentric/hannibal)