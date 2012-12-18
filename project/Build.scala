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
       "org.mockito" % "mockito-core" % "1.9.0" % "test",
       "org.mockito" % "mockito-all" % "1.9.0" % "test"
    ) ++ (hBaseVersion match {
      case "0.90" => Seq(
        "org.apache.hadoop" % "hadoop-core" % "0.20.2-cdh3u4",
        "org.apache.hbase" % "hbase" % "0.90.6-cdh3u4"
      )
      case "0.92" => Seq(
        "org.apache.hadoop" % "hadoop-common" % "2.0.0-cdh4.1.2",
        "org.apache.hbase" % "hbase" % "0.92.1-cdh4.1.2"
      )
      case "0.94" => Seq(
        "org.apache.hadoop" % "hadoop-core" % "1.0.3",
        "org.apache.hbase" % "hbase" % "0.94.1"
      )
    })

    val appResolvers = Seq(
      "Cloudera Public Repository" at "http://repository.cloudera.com/artifactory/cloudera-repos/",
      "Mockito Core" at "http://repo2.maven.org/maven2/org/mockito/mockito-core",
      "Mockito All" at "http://repo2.maven.org/maven2/org/mockito/mockito-all"
    ) 
    
    val projectSettings = Seq( 
      ivyXML :=
    		<dependencies>
    			<exclude module="thrift"/>
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
