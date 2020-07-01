package ai.bleckwen.xgboost

import java.lang.{Double => JDouble, Integer => JInt}
import java.util.{Map => JMap}

import collection.JavaConverters._

/**
 * A generic Feature Vector
 */
trait FVector extends Serializable {
  def get(i: Int): Option[Double]
}

/**
 * Dense Feature vector
 * @param values features data
 * @param treatZeroAsNa considers 0.0 as N/A
 */
final case class DenseFVector(values: Array[Double], treatZeroAsNa: Boolean = true) extends FVector {
  override def get(i: Int): Option[Double] = values(i) match {
    case 0.0 if treatZeroAsNa => None
    case d => Some(d)
  }
}

/**
 * DenseFVector companion object is used to handle Java collections
 */
object DenseFVector {
  def apply(values: Array[JDouble]): DenseFVector = DenseFVector.apply(values.map(_.toDouble))
}

/**
 * Sparse Feature vector
 * @param values a map of position/value
 */
final case class SparseFVector(values: Map[Int, Double]) extends FVector {
  override def get(i: Int): Option[Double] = values.get(i)
}

/**
 * SparseFVector companion object is used to handle Java collections
 */
object SparseFVector {
  def apply(values: JMap[JInt, JDouble]): SparseFVector =
    SparseFVector.apply(values.asScala.map(e => e._1.toInt -> e._2.toDouble).toMap)
}
