package ai.bleckwen.xgboost

import java.io.{DataOutputStream, FileOutputStream}

import ml.dmlc.xgboost4j.java.{DMatrix, XGBoost}
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConverters._
import scala.util.Random
import org.apache.commons.io.IOUtils

class Xgboost4jCompatibilySpec extends WordSpec with Matchers with DoubleTolerant {

  private val trainingDataPath = "/agaricus.txt"
  private val nbTrees = 1000
  private val nbFeatures = 126
  private val rand = Random
  private val nbTests = 1000

  private val trainingData = new DMatrix(this.getClass.getResource(trainingDataPath).getFile)
  private val params = Map("alpha" -> 0.0001, "silent" -> 1, "objective" -> "binary:logistic").mapValues(_.asInstanceOf[AnyRef]).asJava
  private val watches = Map("train" -> trainingData).asJava
  private val booster = XGBoost.train(trainingData, params, nbTrees, watches, null, null, null, 0, null)

  private val xgboostBinary = booster.toByteArray
  lazy val predictor: Predictor = Predictor(xgboostBinary)

  "Predictor" should {

    "load model from a Xgboost4j trained model" in {
      predictor.params.nbFeatures shouldBe nbFeatures
      predictor.trees.size shouldBe nbTrees
      // save binary locally for bench
      writetoFile(xgboostBinary, "generated.model")
    }

    "predict like Xgboost4j on a trained model" in {
      for (_<- 1 to nbTests) {
        val feats = (1 to nbFeatures).map(_ => if (rand.nextBoolean()) 1.0 else 0.0).toArray
        val dmatrix = new DMatrix(feats.map(_.toFloat), 1, nbFeatures)
        predictor.predict(feats).head shouldEqual booster.predict(dmatrix).head.head.toDouble
      }
    }

    "predictContrib like Xgboost4j on a trained model" in {
      for (_<- 1 to nbTests) {
        val feats = (1 to nbFeatures).map(_ => if (rand.nextBoolean()) 1.0 else 0.0).toArray
        val dmatrix = new DMatrix(feats.map(_.toFloat), 1, nbFeatures)
        predictor.predictContrib(feats) shouldEqual booster.predictContrib(dmatrix, 0).head.map(_.toDouble)
      }
    }

    "predict like Xgboost4j on a 0.90 model" in {
      val v090Path = "/v090.model"
      val v090Booster = XGBoost.loadModel(this.getClass.getResourceAsStream(v090Path))
      val v090Predictor  = Predictor(IOUtils.toByteArray(this.getClass.getResourceAsStream(v090Path)))
      for (_ <- 1 to nbTests) {
        val feats = (1 to 17).map(_ => rand.nextDouble()*100).toArray
        val dmatrix = new DMatrix(feats.map(_.toFloat), 1, 17)
        v090Predictor.predict(feats).head shouldEqual v090Booster.predict(dmatrix).head.head.toDouble
        }
      }
  }

  private def writetoFile(bytes: Array[Byte], target: String): Unit = {
    val os  = new DataOutputStream(new FileOutputStream(target))
    os.write(bytes,0, bytes.length)
    os.close()
  }
}

