package ai.bleckwen.xgboost

import org.scalatest.{Matchers, WordSpec}

class FVectorSpec extends WordSpec with Matchers {

  "Dense FVector" should {

    "implement get with treatZeroAsNa" in {
      val v = DenseFVector(Array(0.0, 1.0))
      v.get(0).isDefined shouldBe false
      v.get(1).isDefined shouldBe true
      v.get(1).get shouldBe 1.0
    }

    "implement get without treatZeroAsNa" in {
      val v = DenseFVector(Array(0.0, 1.0), treatZeroAsNa = false)
      v.get(0).isDefined shouldBe true
      v.get(0).get shouldBe 0.0
      v.get(1).get shouldBe 1.0
    }
  }

  "SparseFVector" should {

    "implement get " in {
      val v = SparseFVector(Map(1-> 1.0))
      v.get(0).isDefined shouldBe false
      v.get(1).isDefined shouldBe true
      v.get(1).get shouldBe 1.0
    }
  }
}

