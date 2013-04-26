/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

import models.hbase092.HBaseContext092
import scala.Predef.Class
import models._
import models.hbase.HBaseContext
import play.api._
import play.libs.Akka
import akka.util.duration._
import akka.actor.Props
import actors.UpdateMetricsActor
import scala.Predef.Class
import scala.reflect.New
import scala.Some

object Global extends GlobalSettings {

  override def onStart(app: Application) {

    var hBaseContextClass= Class.forName("models.hbase092.HBaseContext092")
    if (hBaseContextClass == null) {
      hBaseContextClass = Class.forName("models.hbase090.HBaseContext090")
    }
    globals.hBaseContext = hBaseContextClass.newInstance.asInstanceOf[HBaseContext]

    if (app.mode != Mode.Test) {
      Logger.info("Application has started in " + app.mode + "-Mode with " + globals.hBaseContext.toString + ", starting Update-Metrics-Actor")

      LogFile.configure(
        setLogLevelsOnStartup = app.configuration.getBoolean("compactions.set-loglevels-on-startup") == Some(true),
        logLevelUrlPattern = app.configuration.getString("compactions.loglevel-url-pattern").get,
        logFilePathPattern = app.configuration.getString("compactions.logfile-path-pattern").get,
        logFileDateFormat = app.configuration.getString("compactions.logfile-date-format").get,
        logFetchTimeout = app.configuration.getInt("compactions.logfile-fetch-timeout-in-seconds").get,
        initialLookBehindSizeInKBs = app.configuration.getInt("compactions.logfile-initial-look-behind-size-in-kb").get
      )

      val updateMetricsActor = Akka.system.actorOf(Props[UpdateMetricsActor], name = "updateMetricsActor")
      Akka.system.scheduler.schedule(0 seconds, 60 seconds, updateMetricsActor, UpdateMetricsActor.UPDATE_REGION_INFO_METRICS)
      Akka.system.scheduler.scheduleOnce(15 seconds, updateMetricsActor, UpdateMetricsActor.INIT_COMPACTION_METRICS)
      Akka.system.scheduler.schedule(90 seconds, 1 days, updateMetricsActor, UpdateMetricsActor.CLEAN_OLD_METRICS)

    } else {
      Logger.info("Application has started in " + app.mode + "\"-Mode, do not start Update-Metrics-Actor")
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}

package object globals {
  var hBaseContext: HBaseContext = null;// = new HBaseContext092()
}