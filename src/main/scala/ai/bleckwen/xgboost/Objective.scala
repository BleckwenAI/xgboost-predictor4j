package ai.bleckwen.xgboost

trait Objective extends Serializable {
  def apply(values: Array[Double]): Array[Double]
}

case object RegLossObjLogistic extends Objective {
  override def apply(values: Array[Double]): Array[Double] = values.map(x => 1.0 / (1.0 + Math.exp(-x)))
}

object Objective {

  def apply(objName: String): Objective = {
    objName match {
      case "binary:logistic" => RegLossObjLogistic
      case _ => throw new UnsupportedOperationException(s"objective$objName is not supported")
    }
  }
}
