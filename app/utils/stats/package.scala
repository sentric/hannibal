package utils

/**
 *
 */
package object stats {
  def mean[T](item:Traversable[T])(implicit n:Numeric[T]) = {
    n.toDouble(item.sum) / item.size.toDouble
  }

  def variance[T](items:Traversable[T])(implicit n:Numeric[T]) : Double = {
    val itemMean = mean(items)
    val count = items.size
    val sumOfSquares = items.foldLeft(0.0d)((total,item)=>{
      val itemDbl = n.toDouble(item)
      val square = math.pow(itemDbl - itemMean,2)
      total + square
    })
    sumOfSquares / count.toDouble
  }

  def stdDev[T](items:Traversable[T])(implicit n:Numeric[T]) : Double = {
    math.sqrt(variance(items))
  }

  def meanBy[NUM,TYP](items:Traversable[TYP])(ext:(TYP) => NUM)(implicit n:Numeric[NUM]) = {
    items.foldLeft(0.0d)((total,item)=>total + n.toDouble(ext(item))) / items.size
  }

  def varianceBy[A,B](items:Traversable[A])(extractor:(A)=>B)(implicit n:Numeric[B]) : Double = {
    val itemMean = meanBy(items)(extractor)
    val count = items.size
    val sumOfSquares = items.foldLeft(0.0d)((total,item)=>{
      val itemDbl = n.toDouble(extractor(item))
      val square = math.pow(itemDbl - itemMean,2)
      total + square
    })
    sumOfSquares / count.toDouble
  }

  def stdDevBy[A,B](items:Traversable[A])(extractor:(A)=>B)(implicit n:Numeric[B]) : Double = {
    math.sqrt(varianceBy(items)(extractor))
  }
}
