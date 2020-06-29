package ai.bleckwen.xgboost

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.nio.BufferUnderflowException

import org.apache.commons.io.IOUtils
import org.scalatest.{Matchers, WordSpec}

import scala.io.Source

// set logger level: -Dorg.slf4j.simpleLogger.defaultLogLevel=trace

class PredictorSpec extends WordSpec with Matchers with DoubleTolerant {

  private val modelPath = "/binary-logistic.model"
  private val testDataPath = "/agaricus.txt"
  private val predictDataPath = "/binary-logistic.predict.txt"
  private val contribDataPath = "/binary-logistic.contrib.txt"
  private val approxDataPath = "/binary-logistic.approx.txt"
  private val bytes: Array[Byte] = IOUtils.toByteArray(this.getClass.getResourceAsStream(modelPath))
  private lazy val predictor: Predictor = Predictor(bytes)

  private lazy val testLines = Source.fromInputStream(this.getClass.getResourceAsStream(testDataPath)).getLines().take(10).toArray
  private lazy val testData = testLines.iterator.map(toFVector)

  "Predictor" should {

    "load model from buffer and compute tree means" in {
      predictor.trees.size shouldBe 3
      predictor.params.nbFeatures shouldBe 126
      predictor.trees.head.root.mean shouldEqual -0.071013
      predictor.trees(1).root.mean   shouldEqual -0.043310
      predictor.trees(2).root.mean   shouldEqual -0.011160
      predictor.trees.head.maxDepth shouldEqual 4
    }


    "predict from Sparse Vectors" in {
      val expected = Source.fromInputStream(this.getClass.getResourceAsStream(predictDataPath)).getLines().map(_.toDouble)
      (expected zip testData).foreach { x => predictor.predict(x._2).head shouldEqual x._1 }
    }

    "predictContrib Approx from Sparse Vectors" in {
      val expected = Source.fromInputStream(this.getClass.getResourceAsStream(approxDataPath)).getLines()
        .map(l => l.split(",").map(_.toDouble))
      (expected zip testData).foreach(x => predictor.predictApproxContrib(x._2) shouldEqual x._1)
    }

    "predictContrib from Sparse Vectors" in {
      val expected = Source.fromInputStream(this.getClass.getResourceAsStream(contribDataPath)).getLines()
        .map(l => l.split(",").map(_.toDouble))
      (expected zip testData).foreach(x => predictor.predictContrib(x._2) shouldEqual x._1)
    }

    "be serializable with standard OutputStreams" in {
      val byteStream = new ByteArrayOutputStream()
      val oos = new ObjectOutputStream(byteStream)
      oos.writeObject(predictor)
      oos.flush()
      oos.close()
      val serialized = byteStream.toByteArray
      val ois = new ObjectInputStream(new ByteArrayInputStream(serialized))
      val value = ois.readObject
      ois.close()
      value match {
        case p: Predictor =>
          p.trees.size shouldBe predictor.trees.size
          p.trees.head.root.mean shouldEqual predictor.trees.head.root.mean
      }
    }

    "implement serialize" in {
      val bytes = predictor.serialize()
      val p = Predictor.deserialize(bytes)
      p.trees.size shouldBe predictor.trees.size
      p.trees.head.root.mean shouldEqual predictor.trees.head.root.mean
    }


    "fail when build from a small buffer" in {
      val bytes = "some cromg buffer".getBytes
      an [BufferUnderflowException] should be thrownBy Predictor(bytes)
    }
  }

  private def toFVector(line: String): FVector =
    SparseFVector(line.substring(2).split(" ").map(elt => elt.split(":")).map { case Array(x, y) => (x.toInt, y.toDouble) }.toMap)

}

