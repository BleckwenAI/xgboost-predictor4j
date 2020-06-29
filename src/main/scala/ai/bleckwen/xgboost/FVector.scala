package ai.bleckwen.xgboost

import java.lang.{Double => JDouble, Integer => JInt}
import java.util.{Map => JMap}

import collection.JavaConverters._

trait FVector extends Serializable {
  def get(i: Int): Option[Double]
}

final case class DenseFVector(values: Array[Double], treatZeroAsNa: Boolean = true) extends FVector {
  override def get(i: Int): Option[Double] = values(i) match {
    case 0.0 if treatZeroAsNa => None
    case d => Some(d)
  }
}

object DenseFVector {
  def apply(values: Array[JDouble]): DenseFVector = DenseFVector.apply(values.map(_.toDouble))
}

final case class SparseFVector(values: Map[Int, Double]) extends FVector {
  override def get(i: Int): Option[Double] = values.get(i)
}

object SparseFVector {
  def apply(values: JMap[JInt, JDouble]): SparseFVector =
    SparseFVector.apply(values.asScala.map(e => e._1.toInt -> e._2.toDouble).toMap)
}
