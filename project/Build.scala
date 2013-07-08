import sbt._
import Keys._
import PlayProject._
import java.lang.System._

object ApplicationBuild extends Build {

    val appName         = "Hannibal"
    val appVersion      = "1.0-SNAPSHOT"

    val hBaseVersion    =
      if(Seq("0.90", "0.92", "0.94").contains(getenv("HANNIBAL_HBASE_VERSION")))
        getenv("HANNIBAL_HBASE_VERSION")
      else
        "0.90"

    println("Configuring for HBase Version: %s".format(hBaseVersion))

    val appDependencies = Seq(
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
    })

    val appResolvers = Seq(
    )
    
    val projectSettings = Seq( 
      ivyXML :=
    	<dependencies>
          <exclude module="thrift" />
          <exclude module="slf4j-log4j12" />
    	</dependencies>
    )

    val hBaseSourceDirectory = (hBaseVersion match {
      case "0.90" => "hbase/0.90/scala"
      case _ => "hbase/0.92/scala"
    })

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers ++= appResolvers,

      unmanagedSourceDirectories in Compile <++= baseDirectory { base =>
        Seq(
          base / hBaseSourceDirectory
        )
      }
    )

    override lazy val settings = super.settings ++ projectSettings

}
