import com.typesafe.sbt.web.SbtWeb
import sbt._
import Keys._
import java.lang.System._
import play.sbt.Play.autoImport._
import PlayKeys._
import play.sbt.PlayImport._

object ApplicationBuild extends Build {

    val appName         = "Hannibal"
    val appVersion      = "1.0-SNAPSHOT"

    val hBaseVersion    =
      if(Seq("0.90", "0.92", "0.94", "0.96", "0.98", "1.3.0").contains(getenv("HANNIBAL_HBASE_VERSION")))
        getenv("HANNIBAL_HBASE_VERSION")
      else
        "1.3.0"

    println("Configuring for HBase Version: %s".format(hBaseVersion))

    val appDependencies = Seq(
      jdbc,
      "com.typesafe.play" %% "anorm-java8" % "2.4.0-RC2",
      "com.google.guava" % "guava" % "23.0",
      cache,
      json,
      ws,
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
      case "1.3.0" => Seq(
        "org.apache.hbase" % "hbase-common" % "1.3.0",
        "org.apache.hbase" % "hbase-client" % "1.3.0",
        "org.apache.hadoop" % "hadoop-common" % "2.7.1",
        "org.apache.hadoop" % "hadoop-client" % "2.7.1"
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
      case "0.96" => "hbase/0.96/scala"
      case "0.98" => "hbase/0.98/scala"
      case _ => "hbase/1.3.0/scala"
    })


    val main = Project(appName, file(".")).enablePlugins(play.sbt.PlayScala, SbtWeb).settings(

      version := appVersion,

      scalaVersion := "2.11.7",

      libraryDependencies ++= appDependencies,
      libraryDependencies += specs2 % Test,
      libraryDependencies += evolutions,

      unmanagedSourceDirectories in Compile <++= baseDirectory { base =>
        Seq(
          base / hBaseSourceDirectory
        )
      }
    )

    override lazy val settings = super.settings ++ projectSettings

}
