package ai.bleckwen.xgboost

import org.scalactic.{Equality, TolerantNumerics}

trait DoubleTolerant extends TolerantNumerics {
    private val tolerance = 1e-5

    implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(tolerance)

    implicit val arrayEquality: Equality[Array[Double]] = new Equality[Array[Double]]{
        override def areEqual(a: Array[Double], b: Any): Boolean = b match {
            case bArray: Array[Double] => (a zip bArray).forall(x => Math.abs(x._1 - x._2) <= tolerance)
            case _ => false
        }
    }
}
