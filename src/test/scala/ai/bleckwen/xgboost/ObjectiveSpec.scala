package ai.bleckwen.xgboost

import org.scalatest.{Matchers, WordSpec}

class ObjectiveSpec extends WordSpec with Matchers with DoubleTolerant {

  "Objective" should {

    "implement RegLossObjLogistic" in {
      val obj = Objective("binary:logistic")
      obj(Array(0.0, 1e10)) shouldBe Array(0.5, 1.0)
    }
  }
}

