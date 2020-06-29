package ai.bleckwen.xgboost

import java.nio.ByteBuffer

import org.apache.commons.io.IOUtils
import org.scalatest.{Matchers, WordSpec}

class ModelParamsSpec extends WordSpec with Matchers {

  private val modelPath= "/binary-logistic.model"

  "ModelParams" should {

    "load parameters from buffer" in {
      val buffer = ByteBuffer.wrap(IOUtils.toByteArray(this.getClass.getResourceAsStream(modelPath)))
      val params = ModelParams(buffer)
      buffer.getInt // skip num_roots
      val nbNodes = buffer.getInt
      params.baseScore shouldBe 0.0
      params.nbFeatures shouldBe 126
      params.nbClass shouldBe 0
      params.nbTrees shouldBe 3
      params.nbGroups shouldBe 1
      params.objName shouldEqual "binary:logistic"
      nbNodes shouldBe 19 // ensure that padding is correct
    }

    "implement serialize" in {
      val params = ModelParams(2.0f, 1, 2,3,4, "someObject")
      val deserialized = ModelParams(params.serialize())
      deserialized shouldEqual params
    }
  }
}

