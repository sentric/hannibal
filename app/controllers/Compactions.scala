/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package controllers

import play.api.mvc._
import utils.MetricUtil

object Compactions extends Controller {
  def index = Action { implicit request =>
    Ok(views.html.compactions.index(MetricUtil.findLongestCompactionInLastWeek()))
  }
}