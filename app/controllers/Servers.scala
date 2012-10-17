package controllers

import play.api.mvc._

object Servers extends Controller {
  def index = Action {
    Ok(views.html.servers.index())
  }
}