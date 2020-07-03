package ai.bleckwen.xgboost

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.lang.{Double => JDouble, Integer => JInt}
import java.util.{Map => JMap}

/**
 * Predictor fox XGboost
 * @param params model parameters
 * @param trees  decision tree
 * @param objective objective function
 */
@SerialVersionUID(1L)
// do not add a final as we are mocking this class in our tests
case class Predictor(params: ModelParams, trees: Seq[DecisionTree], objective: Objective) {

  /**
   * Predict score
   * @param vector input FVector
   * @return scores (single element in case of binary classification)
   */
  def predict(vector: FVector): Array[Double] = {
    var score = 0.0
    trees.foreach(score += _.getScore(vector))
    objective(Array(score))
  }

  /**
   * Predict score
   * @param values  input data (dense). 0.0 is considered as N/A
   * @return scores (single element in case of binary classification)
   */
  def predict(values: Array[Double]): Array[Double] = predict(DenseFVector(values))

  /**
   * Predict score
   * @param values  input data (sparse, first element is the position)
   * @return scores (single element in case of binary classification)
   */
  def predict(values: Map[Int, Double]): Array[Double] = predict(SparseFVector(values))

  /**
   * Output feature contributions toward predictions of given data using SHAP algorithm
   * @param vector input FVector
   * @return feature contributions
   */
  def predictContrib(vector: FVector): Array[Double] = {
    val contribs = Array.fill[Double](params.nbFeatures + 1)(0.0)
    trees.foreach(_.computeContrib(vector, contribs))
    contribs
  }

  /**
   * Output feature contributions toward predictions of given data using SHAP algorithm
   * @param values  input data (dense). 0.0 is considered as N/A
   * @return feature contributions
   */
  def predictContrib(values: Array[Double]): Array[Double] = predictContrib(DenseFVector(values))

  /**
   * Output feature contributions toward predictions of given data using SHAP algorithm
   * @param values  input data (sparse, first element is the position)
   * @return feature contributions
   */
  def predictContrib(values: Map[Int, Double]): Array[Double] = predictContrib(SparseFVector(values))

  /**
   * Output feature contributions toward predictions of given data using approximation (means) algorithm
   * @param vector input FVector
   * @return feature contributions
   */
  def predictApproxContrib(vector: FVector): Array[Double] = {
    val contribs = Array.fill[Double](params.nbFeatures + 1)(0.0)
    trees.foreach(_.computeApproxContrib(vector, contribs))
    contribs
  }

  /**
   * Output feature contributions toward predictions of given data using approximation (means) algorithm
   * @param values  input data (dense). 0.0 is considered as N/A
   * @return feature contributions
   */
  def predictApproxContrib(values: Array[Double]): Array[Double] = predictApproxContrib(DenseFVector(values))

  /**
   * Output feature contributions toward predictions of given data using approximation (means) algorithm
   * @param values  input data (sparse, first element is the position)
   * @return feature contributions
   */
  def predictApproxContrib(values: Map[Int, Double]): Array[Double] = predictApproxContrib(SparseFVector(values))

  /* Java APIs: same signature than Scala API but with JAVA collections
   */
  def predict(values: Array[JDouble]): Array[JDouble] = predict(DenseFVector(values)).map(Double.box)
  def predict(values: JMap[JInt, JDouble]): Array[JDouble] = predict(SparseFVector(values)).map(Double.box)

  def predictContrib(values: Array[JDouble]): Array[JDouble] = predictContrib(DenseFVector(values)).map(Double.box)
  def predictContrib(values: JMap[JInt, JDouble]): Array[JDouble] = predictContrib(SparseFVector(values)).map(Double.box)

  def predictApproxContrib(values: Array[JDouble]): Array[JDouble] = predictApproxContrib(DenseFVector(values)).map(Double.box)
  def predictApproxContrib(values: JMap[JInt, JDouble]): Array[JDouble] = predictApproxContrib(SparseFVector(values)).map(Double.box)

  /**
   * Serialize the Predictor using internal format (this is NOT an Xgboost binary)
   * @return array of bytes
   */
  def serialize() : Array[Byte] = {
    val paramsBytes = params.serialize()
    val treesBytes = trees.map(_.serialize)
    val buffer = ByteBuffer.allocate(Predictor.sizeOf(paramsBytes) + treesBytes.map(Predictor.sizeOf).sum)
    Predictor.putBytes(buffer, paramsBytes)
    treesBytes.foreach(Predictor.putBytes(buffer,_))
    buffer.array
  }

}

object Predictor {
  private val nodeFactory = NodeFactoryImpl

  /**
   * Create a Predictor from an Xgboost binary
   * @param modelBinary Xgboost binary
   * @return Predictor
   */
  def apply(modelBinary: Array[Byte]): Predictor = {
    val buffer = ByteBuffer.wrap(modelBinary)
    val params = ModelParams(buffer)
    val trees = (1 to params.nbTrees).map(_ => DecisionTree(nodeFactory, RawNode.readTree(buffer)))
    Predictor(params, trees, Objective(params.objName))
  }

  /**
   * Deserialize the Predictor using internal
   * @param bytes serialized data
   * @return Predictor
   */
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


/**
 * Builder to ease the creation of Predictor from Java (avoid using Scala Module name)
 */
class PredictorBuilder() {

  @throws(classOf[BufferUnderflowException])
  def build(modelBinary: Array[Byte]): Predictor = Predictor(modelBinary)

}
