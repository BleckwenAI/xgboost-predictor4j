package ai.bleckwen.xgboost

import java.nio.ByteBuffer

import scala.collection.mutable

/**
 * The node implementation (actually a simple case class)
 * @param left left child
 * @param right right child
 * @param id rw node id (just used for information)
 * @param index split index
 * @param defaultLeft is left child the default
 * @param value node value
 * @param sumHess sum hess
 * @param mean mean value
 */
final case class NodeImpl(override val left: Node,
                          override val right: Node,
                          id: Int,
                          index: Int,
                          defaultLeft: Boolean,
                          value: Double,
                          sumHess: Double,
                          var mean: Double = 0.0)
  extends Node(left, right) {

  override def setMean(v: Double): Unit = this.mean = v

  def serialize(fullTree: Boolean = false): Array[Byte] = {
    if (fullTree) {
      ByteBuffer.allocate(java.lang.Integer.BYTES).putInt(size).array() ++ serialize()
    } else if (isLeaf) serializeNode
      else left.serialize() ++ right.serialize() ++ serializeNode
  }

  private def serializeNode: Array[Byte] = {
    val buffer = ByteBuffer.allocate(5 * java.lang.Integer.BYTES + 3 * java.lang.Double.BYTES)
    buffer.putInt(id)
    buffer.putInt(left.id)
    buffer.putInt(right.id)
    buffer.putInt(index)
    buffer.putInt(if (defaultLeft) 1 else 0)
    buffer.putDouble(value)
    buffer.putDouble(sumHess)
    buffer.putDouble(mean)
    buffer.array
  }

  override def toString: String = f"Node@${Integer.toHexString(hashCode)}($id)"
}

case object NodeFactoryImpl extends NodeFactory {

  override def fromRaw(nodes: IndexedSeq[RawNode]): Node = {
    def build(raw: RawNode): NodeImpl = {
      if (raw.isLeaf) NodeImpl(NilNode, NilNode, raw.id, 0, defaultLeft = true, raw.value, raw.sumHess)
      else NodeImpl(build(nodes(raw.left)),  build(nodes(raw.right)), raw.id, raw.splitIndex, raw.defaultLeft, raw.value, raw.sumHess)
    }
    build(nodes(0))
  }

  override def fromBytes(bytes: Array[Byte]): Node = {
    val buffer = ByteBuffer.wrap(bytes)
    val treeSize = buffer.getInt
    val nodesMap = mutable.Map[Int, Node]()
    var node: Node = NilNode
    for (_ <- 1 to treeSize) {
      val nodeId = buffer.getInt
      node = NodeImpl(
        nodesMap.getOrElse(buffer.getInt, NilNode),
        nodesMap.getOrElse(buffer.getInt, NilNode),
        nodeId,
        buffer.getInt,
        buffer.getInt == 1,
        buffer.getDouble,
        buffer.getDouble,
        buffer.getDouble)
      nodesMap.put(nodeId, node)
    }
    // the last serialized node is the root node
    node
  }
}
