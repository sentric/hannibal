package controllers

import play.api.mvc._
import models.Table
import com.codahale.jerkson.Json

object Tables extends Controller {
  def index() = Action {
    Ok(views.html.tables.index(Table.all()))
  }

  def show(tableName:String) = Action {
    Ok(views.html.tables.show(tableName))
  }
}