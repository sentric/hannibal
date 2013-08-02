/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

import models.hbase.HBaseContext
import play.api._
import actors.UpdateMetricsActor

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    try {
      globals.hBaseContext = Class.forName("models.hbase092.HBaseContext092").newInstance.asInstanceOf[HBaseContext]
    } catch {
      case e: java.lang.ClassNotFoundException =>
        globals.hBaseContext = Class.forName("models.hbase090.HBaseContext090").newInstance.asInstanceOf[HBaseContext]
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
