package ai.bleckwen.xgboost

import java.nio.{BufferUnderflowException, ByteBuffer}
import java.lang.{Double => JDouble, Integer => JInt}
import java.util.{Map => JMap}

// warning: Predictor cannot be final as we use mockito v1
case class Predictor(params: ModelParams, trees: Seq[DecisionTree], objective: Objective) {
  import Predictor._

  def predict(vector: FVector): Array[Double] = {
    var score = 0.0
    trees.foreach(score += _.getScore(vector))
    objective(Array(score))
  }

  def predict(values: Array[Double]): Array[Double] = predict(DenseFVector(values))
  def predict(values: Map[Int, Double]): Array[Double] = predict(SparseFVector(values))

  def predictContrib(vector: FVector): Array[Double] = {
    val contribs = Array.fill[Double](params.nbFeatures + 1)(0.0)
    trees.foreach(_.computeContrib(vector, contribs))
    contribs
  }

  def predictContrib(values: Array[Double]): Array[Double] = predictContrib(DenseFVector(values))
  def predictContrib(values: Map[Int, Double]): Array[Double] = predictContrib(SparseFVector(values))

  def predictApproxContrib(vector: FVector): Array[Double] = {
    val contribs = Array.fill[Double](params.nbFeatures + 1)(0.0)
    trees.foreach(_.computeApproxContrib(vector, contribs))
    contribs
  }

  def predictApproxContrib(values: Array[Double]): Array[Double] = predictApproxContrib(DenseFVector(values))
  def predictApproxContrib(values: Map[Int, Double]): Array[Double] = predictApproxContrib(SparseFVector(values))

  // Java interfaces
  def predict(values: Array[JDouble]): Array[JDouble] = predict(DenseFVector(values)).map(Double.box)
  def predict(values: JMap[JInt, JDouble]): Array[JDouble] = predict(SparseFVector(values)).map(Double.box)

  def predictContrib(values: Array[JDouble]): Array[JDouble] = predictContrib(DenseFVector(values)).map(Double.box)
  def predictContrib(values: JMap[JInt, JDouble]): Array[JDouble] = predictContrib(SparseFVector(values)).map(Double.box)

  def predictApproxContrib(values: Array[JDouble]): Array[JDouble] = predictApproxContrib(DenseFVector(values)).map(Double.box)
  def predictApproxContrib(values: JMap[JInt, JDouble]): Array[JDouble] = predictApproxContrib(SparseFVector(values)).map(Double.box)

  def serialize() : Array[Byte] = {
    val paramsBytes = params.serialize()
    val treesBytes = trees.map(_.serialize())
    val buffer = ByteBuffer.allocate(sizeOf(paramsBytes) + treesBytes.map(sizeOf).sum)
    putBytes(buffer, paramsBytes)
    treesBytes.foreach(putBytes(buffer,_))
    buffer.array
  }
}

object Predictor {
  private val nodeFactory = NodeFactoryImpl

  def apply(modelBinary: Array[Byte]): Predictor = {
    val buffer = ByteBuffer.wrap(modelBinary)
    val params = ModelParams(buffer)
    val trees = (1 to params.nbTrees).map(_ => DecisionTree(nodeFactory, RawNode.readTree(buffer)))
    Predictor(params, trees, Objective(params.objName))
  }

  def deserialize(bytes: Array[Byte]): Predictor = {
    val buffer = ByteBuffer.wrap(bytes)
    val params = ModelParams(getBytes(buffer))
    val trees = (1 to params.nbTrees).map(_ => DecisionTree(nodeFactory.fromBytes(getBytes(buffer))))
    Predictor(params, trees, Objective(params.objName))
  }

  private def sizeOf(bytes: Array[Byte]): Int = java.lang.Integer.BYTES + bytes.length

  private def getBytes(buffer: ByteBuffer): Array[Byte] = {
    val length = buffer.getInt
    val output = new Array[Byte](length)
    buffer.get(output)
    output
  }

  private def putBytes(buffer: ByteBuffer, bytes: Array[Byte]): Unit = {
    buffer.putInt(bytes.length)
    buffer.put(bytes)
  }
}

// this class is to ease integration with Kotlin and Java
class PredictorBuilder() {

  @throws(classOf[BufferUnderflowException])
  def build(modelBinary: Array[Byte]): Predictor = Predictor(modelBinary)

}
