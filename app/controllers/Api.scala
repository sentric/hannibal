/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
package controllers

import play.api.mvc._
import play.api.libs.json.Json._

object Api extends Controller {

  def heartbeat = Action { implicit request =>
    Ok(stringify(toJson(Map(
      "status" -> toJson("OK")
    )))).as("application/json")
  }
}
