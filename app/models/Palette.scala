package models

import scala.util.Random
import scala.collection.mutable.HashMap

class Palette(baseColor: Color = new Color(0.5F, 0.5F, 0.5F)) {
  val keyedColors: HashMap[String, Color] = new HashMap()

  // Ported from http://stackoverflow.com/questions/43044/algorithm-to-randomly-generate-an-aesthetically-pleasing-color-palette/43235#43235
  def nextColor = {
    var (red, green, blue) = (generateComponent('red), generateComponent('green), generateComponent('blue))

    Color(red, green, blue)
  }

  def generateComponent(component: Symbol) = {
    val mixinComponent = Random.nextFloat
    val baseComponent = component match {
      case 'red   => baseColor.red
      case 'green => baseColor.green
      case 'blue  => baseColor.blue
      case _      => mixinComponent
    }

    (baseComponent + mixinComponent) / 2
  }

  def getColor(key: String) = {
    if (!keyedColors.contains(key)) {
      keyedColors.put(key, nextColor);
    }
    
    keyedColors.get(key).get
  }
}

object Palette {
  val palette = new Palette()
  def getColor(key: String) = palette.getColor(key)
}