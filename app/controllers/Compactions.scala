/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package controllers

import play.api.mvc._
import utils.MetricUtil
import java.text.DateFormat

object Compactions extends Controller {
  def index = Action { implicit request =>
    val longestCompactionString = MetricUtil.findLongestCompactionInLastWeek() match {
      case Some(longestCompaction) =>
        "<b>%.1fs</b> at %s <a href='%s'>on Region</a>".format(
          longestCompaction._1/1000.0,
          DateFormat.getInstance().format(longestCompaction._2),
          routes.Regions.show(longestCompaction._3)
        )
      case None =>
        "None"
    }
    Ok(views.html.compactions.index(longestCompactionString))
  }
}