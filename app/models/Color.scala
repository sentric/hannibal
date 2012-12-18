/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */

package models

case class Color(red: Float, green: Float, blue: Float) {
  def this(red: Int, green: Int, blue: Int) = this(red.toFloat / 255, blue.toFloat / 255, green.toFloat / 255)

  def redInt: Int = (red * 255).round
  def blueInt: Int = (blue * 255).round
  def greenInt: Int = (green * 255).round

  def toHtmlCode = "#%02x%02x%02x".format(redInt, blueInt, greenInt)
}