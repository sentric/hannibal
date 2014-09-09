import sbt._
import Keys._
import java.lang.System._

object ApplicationBuild extends Build {

    val appName         = "Hannibal"
    val appVersion      = "1.0-SNAPSHOT"

    val hBaseVersion    =
      if(Seq("0.90", "0.92", "0.94", "0.96", "0.98").contains(getenv("HANNIBAL_HBASE_VERSION")))
        getenv("HANNIBAL_HBASE_VERSION")
      else
        "0.90"

    println("Configuring for HBase Version: %s".format(hBaseVersion))

    val appDependencies = Seq(
//      jdbc,
//      anorm,
//      cache,
//      ws,
        "org.slf4j" % "slf4j-log4j12" % "1.6.0"
    ) ++ (hBaseVersion match {
      case "0.90" => Seq(
        "org.apache.hadoop" % "hadoop-core" % "0.20.205.0",
        "org.apache.hbase" % "hbase" % "0.90.5"
      )
      case "0.92" => Seq(
        "org.apache.hadoop" % "hadoop-core" % "0.20.205.0",
        "org.apache.hbase" % "hbase" % "0.92.2"
      )
      case "0.94" => Seq(
        "org.apache.hadoop" % "hadoop-core" % "0.20.205.0",
        "org.apache.hbase" % "hbase" % "0.94.3"
      )
      case "0.96" => Seq(
        "org.apache.hadoop" % "hadoop-common" % "2.4.0",
        "org.apache.hbase" % "hbase-common" % "0.96.2-hadoop2",
        "org.apache.hbase" % "hbase-client" % "0.96.2-hadoop2"
      )
      case "0.98" => Seq(
        "org.apache.hadoop" % "hadoop-common" % "2.4.0",
        "org.apache.hbase" % "hbase-common" % "0.98.1-hadoop2",
        "org.apache.hbase" % "hbase-client" % "0.98.1-hadoop2"
      )
    })

    val projectSettings = Seq(
      ivyXML :=
    	<dependencies>
          <exclude module="thrift" />
          <exclude module="slf4j-log4j12" />
    	</dependencies>
    )

      val hBaseSourceDirectory = (hBaseVersion match {
      case "0.90" => "hbase/0.90/scala"
      case "0.92" => "hbase/0.92/scala"
      case "0.94" => "hbase/0.92/scala"
      case _ => "hbase/0.96/scala"
    })


    val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(

      version := appVersion,

      scalaVersion := "2.11.1",

      libraryDependencies ++= appDependencies,

      unmanagedSourceDirectories in Compile <++= baseDirectory { base =>
        Seq(
          base / hBaseSourceDirectory
        )
      }
    )

    override lazy val settings = super.settings ++ projectSettings

}