/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action { implicit request =>
    Redirect(routes.Servers.index())
  }

}