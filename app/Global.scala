/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

import models.hbase.HBaseContext
import play.api._
import actors.UpdateMetricsActor
import java.util.regex.Pattern

object Global extends GlobalSettings {

  private val apiVersions = List(
    "models.hbase096.HBaseContext096",
    "models.hbase092.HBaseContext092",
    "models.hbase090.HBaseContext090"
  );

  override def onStart(app: Application) {
    apiVersions.foreach { hbaseContext:String =>
      if(globals.hBaseContext == null) {
        try {
          Logger.debug("Try to intanciate api-wrapper %s".format(hbaseContext));
          globals.hBaseContext = Class.forName(hbaseContext).newInstance.asInstanceOf[HBaseContext]
          val overrideCompactionRegex = app.configuration.getString("logfileParser.overrideCompactionRegexPattern").getOrElse("")
          if(!Option(overrideCompactionRegex).getOrElse("").isEmpty){
            //override regex  pattern for compaction metric has been set by user
            val overrideRegexPattern = Pattern.compile(overrideCompactionRegex,Pattern.MULTILINE)
            globals.hBaseContext.logFileParser.setOverrideCopactionRegexPattern(overrideRegexPattern)
            Logger.info("Setting regex pattern for Compaction metrics in logFileParser as [%s]".format(overrideRegexPattern.pattern()))
          }
        } catch {
          case e: java.lang.ClassNotFoundException =>
            Logger.debug("Instanciating api-wrapper %s failed ".format(hbaseContext));
        }
      }
    }
    if(globals.hBaseContext == null) {
      Logger.error("Could not instanciate any api wrapper, Hannibal will now exit");
      System.exit(1);
    }

    if (app.mode != Mode.Test) {
      Logger.info("Application has started in " + app.mode + "-Mode with " + globals.hBaseContext.toString + ", starting Update-Metrics-Actor")
      UpdateMetricsActor.initialize( app.configuration )

    } else {
      Logger.info("Application has started in " + app.mode + "\"-Mode, do not start Update-Metrics-Actor")
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }
}

package object globals {
  var hBaseContext: HBaseContext = null
}
