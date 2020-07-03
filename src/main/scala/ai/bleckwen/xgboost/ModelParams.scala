package ai.bleckwen.xgboost

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * the model Parameters
 * @param baseScore base score
 * @param nbFeatures number of features
 * @param nbClass number of classes
 * @param nbTrees number of trees
 * @param nbGroups numbet of groups
 * @param objName objective name
 */
final case class ModelParams(baseScore: Float, nbFeatures: Int, nbClass: Int, nbTrees: Int, nbGroups: Int, objName: String) {

  def serialize(): Array[Byte] = {
    val buffer = ByteBuffer.allocate(java.lang.Float.BYTES + 4 * java.lang.Integer.BYTES + ModelParams.sizeOf(objName))
    buffer.putFloat(baseScore)
    buffer.putInt(nbFeatures)
    buffer.putInt(nbClass)
    buffer.putInt(nbTrees)
    buffer.putInt(nbGroups)
    ModelParams.putString(buffer, objName)
    buffer.array()
  }
}

object ModelParams {

  def apply(buffer: ByteBuffer): ModelParams = {
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    val baseScore = buffer.getFloat
    val nbFeatures = buffer.getInt
    val nbClass = buffer.getInt // multi-class classification
    padding(buffer,31)
    val objName = getString(buffer)
    val modelType = getString(buffer)
    if (modelType != "gbtree")
      throw new UnsupportedOperationException(s"model Type $modelType is not supported")
    val nbTrees = buffer.getInt
    buffer.getInt // num_Roots not used
    buffer.getInt // nbFeats
    padding(buffer,1)
    buffer.getLong // num_pbuffer ignored
    var nbGroups = buffer.getInt
    if (nbGroups > 1)
      throw new UnsupportedOperationException(s"nbGroups $nbGroups is not supported")
    if (nbGroups == 0)
      nbGroups = 1
    buffer.getInt // size_leaf_vector ignored
    padding(buffer,32)
    ModelParams(baseScore, nbFeatures, nbClass, nbTrees, nbGroups, objName)
  }

  def apply(bytes: Array[Byte]): ModelParams = {
    val buffer = ByteBuffer.wrap(bytes)
    ModelParams(buffer.getFloat, buffer.getInt, buffer.getInt, buffer.getInt, buffer.getInt, getString(buffer))
  }

  private def sizeOf(str: String): Int = java.lang.Long.BYTES + str.length

  private def getString(buffer: ByteBuffer): String = {
    val a = new Array[Byte](buffer.getLong.toInt)
    buffer.get(a)
    new String(a)
  }

  private def putString(buffer: ByteBuffer, str: String): Unit = {
    buffer.putLong(str.length)
    buffer.put(str.getBytes())
  }

  private def padding(buffer: ByteBuffer, n: Int): Unit = {
    for (_ <- 1 to n) buffer.getInt
  }
}

