/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

import models.{Compaction, MetricDef}
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.core.StaticApplication
import play.db.DB
import play.libs.Akka
import akka.util.duration._
import akka.actor.Props
import actors.UpdateMetricsActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    if(app.mode != Mode.Test) {
      Logger.info("Application has started in "+app.mode+"-Mode, starting Update-Metrics-Actor")

      Compaction.configure(
        setLogLevelsOnStartup = app.configuration.getBoolean("compactions.set-loglevels-on-startup") == Some(true),
        logLevelUrlPattern = app.configuration.getString("compactions.loglevel-url-pattern").get,
        logFileUrlPattern = app.configuration.getString("compactions.logfile-url-pattern").get
      )

      val updateMetricsActor = Akka.system.actorOf(Props[UpdateMetricsActor], name = "updateMetricsActor")
      Akka.system.scheduler.schedule(0 seconds, 60 seconds, updateMetricsActor, UpdateMetricsActor.UPDATE_REGION_INFO_METRICS)
      Akka.system.scheduler.scheduleOnce(15 seconds, updateMetricsActor, UpdateMetricsActor.INIT_COMPACTION_METRICS)
      Akka.system.scheduler.schedule(30 seconds, 600 seconds, updateMetricsActor, UpdateMetricsActor.UPDATE_COMPACTION_METRICS)
      Akka.system.scheduler.schedule(90 seconds, 1 days, updateMetricsActor, UpdateMetricsActor.CLEAN_OLD_METRICS)

    } else {
      Logger.info("Application has started in "+app.mode+"\"-Mode, do not start Update-Metrics-Actor")
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}