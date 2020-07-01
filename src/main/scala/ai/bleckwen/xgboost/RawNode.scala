package ai.bleckwen.xgboost

import java.nio.{ByteBuffer, ByteOrder}

/**
 * RawNode is just use to read the trees from binary model file
 * @param id node id
 * @param left left node id
 * @param right right node id
 * @param isLeaf is this a leaf node
 * @param value leaf value or split value
 * @param defaultLeft is left child the default
 * @param splitIndex split index
 * @param sumHess sum Hess
 */
final case class RawNode(id: Int, left: Int, right: Int, isLeaf: Boolean, value: Float, defaultLeft: Boolean, splitIndex: Int, var sumHess: Float)

object RawNode  {
  def apply(id: Int, buffer: ByteBuffer): RawNode = {
    buffer.getInt // parent
    val left = buffer.getInt
    val isLeaf = left == -1
    val right = buffer.getInt
    val sindex = buffer.getInt
    val value = buffer.getFloat
    val defaultLeft = (sindex >>> 31) != 0
    val splitIndex = (sindex & ((1L << 31) - 1L)).toInt
    val node = RawNode(id, left, right, isLeaf, value, defaultLeft, splitIndex, 0f)
    node
  }

  def readTree(buffer: ByteBuffer): IndexedSeq[RawNode] = {
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.getInt // num_roots
    val nbNodes = buffer.getInt
    buffer.getInt // num_deleted
    buffer.getInt // max_depth
    buffer.getInt // num_feature
    buffer.getInt // size_leaf_vector
    for (_ <- 1 to 31)
      buffer.getInt
    val nodes = (0 until nbNodes).map(RawNode(_, buffer))
    // read stats and update sum Hess
    nodes.foreach{ node =>
        buffer.getFloat  // loss_chg
        val sumHess = buffer.getFloat
        buffer.getFloat // base_weight
        buffer.getInt // leaf_child_cnt
        node.sumHess = sumHess
      }
    nodes
  }
}
