# ![Hannibal](https://github.com/sentric/hannibal/blob/master/public/images/hannibal-logo-large-white.png?raw=true)

Hannibal is a tool to help monitor and maintain [HBase](http://hbase.apache.org)-Clusters that are configured for
[manual splitting](http://hbase.apache.org/book/important_configurations.html#disable.splitting).

While HBase provides metrics to monitor overall cluster health via JMX or Ganglia, it lacks the ability to monitor
single regions in an easy way. This information is essential when your cluster is configured for manual splits,
especially when the data growth is not uniform.

This tool tries to fill that gap by answering the following questions:
 - How well are regions balanced over the cluster?
 - How well are the regions split for each table?
 - How do regions evolve over time?

## Requirements & Limitations

The current version is tested on HBase 0.90.x in distributed mode only. Support for 0.92.x is planned and will be
available soon.

Java 6 JDK is required on the machine where this tool is built.

## Quickstart

1. Grab the sources from github: `git clone https://github.com/sentric/hannibal.git`

2. Copy `<project>/conf/hbase-site.template.xml` to `<project>/conf/hbase-site.xml` and adjust it.

3. Run the start script inside the root folder of the project: `./start`

The first time you start, the script will fetch all requirements to build and run the the application via
[sbt](http://www.scala-sbt.org/), so it will take a few minutes to build.

When the application has started, you can access the WEB UI at: <http://localhost:9000>

Please note that history data about regions is only collected while the application is running, it will need to run for
some time until the region detail graphs fill up. 

For information about the usage, check out our [Wiki][wu].

## How to display compactions

HBase 0.90.x's API doesn't allow you to query information on running compactions directly, so what we do is to parse
the RegionServers' log files directly, which are available through the service interface.
For this to work, the log levels need to be set at least to `INFO`.

Hannibal can set the log level to `INFO` for you, just edit [conf/application.conf](blob/master/conf/application.conf)
and set

    init.set_hbase_loglevels_to_info = true

## More Information

More information about the tool can be found in the [Wiki][w]

## Additional Features

If you need additional features please get in [touch with us](http://sentric.ch/contact), maybe we can work something
out.

## License

Hannibal is released under MIT License, see [LICENSE][] for details.

## Contributors

This tool is developed at [Sentric][] by: [Nils Kübler][] and [Jiayong Ou][]

With help from: [Jean-Pierre König][], [Christian Gügi][] and [Vadim Kisselmann][], Ben Taylor, Stephanie Höhn

 [Sentric]: http://www.sentric.ch
 [Nils Kübler]: https://twitter.com/nkuebler
 [Jiayong Ou]: https://twitter.com/jiayongou
 [Jean-Pierre König]: https://twitter.com/jpkoenig
 [Christian Gügi]: https://twitter.com/cguegi
 [Vadim Kisselmann]: https://twitter.com/vkisselmann
 [w]: https://github.com/sentric/hannibal/wiki
 [wu]: https://github.com/sentric/hannibal/wiki/Usage
 [LICENSE]: https://github.com/sentric/hannibal/blob/master/LICENSE

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/ed9e66b101612798e3e015369f86b502 "githalytics.com")](http://githalytics.com/sentric/hannibal)
