package ai.bleckwen.xgboost

import java.nio.ByteBuffer

import scala.collection.mutable

/*
Implementation of TreeNode with a simple Case Class
 */
final case class NodeImpl(override val left: Node,
                          override val right: Node,
                          index: Int,
                          defaultLeft: Boolean,
                          value: Double,
                          hess: Double,
                          var mean: Double = 0.0)
  extends Node(left, right) {
  import NodeFactoryImpl.serializedIds

  override def setMean(mean: Double): Unit = this.mean = mean

  def serialize(fullTree: Boolean = false): Array[Byte] = {
    if (fullTree) {
      serializedIds.clear()
      ByteBuffer.allocate(java.lang.Integer.BYTES).putInt(size).array() ++ serialize()
    } else if (isLeaf) serializeNode()
      else left.serialize() ++ right.serialize() ++ serializeNode()
  }

  private def serializeNode(): Array[Byte] = {
    val nodeId = serializedIds.size + 1
    serializedIds.put(this, nodeId)
    val buffer = ByteBuffer.allocate(5 * java.lang.Integer.BYTES + 3 * java.lang.Double.BYTES)
    buffer.putInt(nodeId)
    buffer.putInt(serializedIds.getOrElse(left, -1))
    buffer.putInt(serializedIds.getOrElse(right, -1))
    buffer.putInt(index)
    buffer.putInt(if (defaultLeft) 1 else 0)
    buffer.putDouble(value)
    buffer.putDouble(hess)
    buffer.putDouble(mean)
    buffer.array
  }

  override def toString: String = f"NodeImpl($index,$defaultLeft,$value,$hess)"
}

case object NodeFactoryImpl extends NodeFactory {
  val serializedIds: mutable.Map[Node, Int] = mutable.Map.empty

  override def fromRaw(nodes: IndexedSeq[RawNode]): Node = {
    def build(raw: RawNode): NodeImpl = {
      if (raw.isLeaf) NodeImpl(NilNode, NilNode, 0, defaultLeft = true, raw.value, raw.sumHess)
      else NodeImpl(build(nodes(raw.left)),  build(nodes(raw.right)), raw.splitIndex, raw.defaultLeft, raw.value, raw.sumHess)
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
