package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

    def index = Action {
    	val regionList = models.Region.all
    	Redirect(routes.Servers.index())
    }

}