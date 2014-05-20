/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package controllers

import play.api.mvc._
import models.Table
import views.html.{tables => html}

object Tables extends Controller {
  def index() = Action { implicit request =>
    Ok(html.index(Table.all()))
  }

  def show(tableName: String) = Action { implicit request =>
    Table.findByName(tableName) map {table =>
      Ok(html.show(table))
    } getOrElse NotFound
  }
}
