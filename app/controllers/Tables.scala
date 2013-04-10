/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package controllers

import play.api.mvc._
import models.Table
import com.codahale.jerkson.Json._

object Tables extends Controller {
  def index() = Action { implicit request =>
    Ok(views.html.tables.index(Table.all()))
  }

  def show(tableName:String) = Action { implicit request =>
    val table = Table.findByName(tableName)
    if(table == null)
      NotFound
    else
      Ok(views.html.tables.show(table))
  }
}