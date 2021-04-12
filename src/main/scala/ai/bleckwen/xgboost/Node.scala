package ai.bleckwen.xgboost

/**
 * A generic (binary) node of a decision Tree
 * @param left left node child (NilNode in case of leaf)
 * @param right right node child (NilNode in case of leaf)
 *
 * Note: this pattern is a bit unsual. The good practice would be to define a Leaf object rather than a Nil
 * (with null fields) but this solution speeds up processing as we don't need to match case classes when
 * descending in the tree
 */
abstract class Node(val left: Node, val right: Node) extends Serializable {
  def defaultLeft: Boolean
  def value: Double
  def id: Int
  def index: Int
  def mean: Double
  def sumHess: Double
  def setMean(v: Double)
  def serialize(fullTree: Boolean = false): Array[Byte]

  lazy val isLeaf: Boolean = left == NilNode

  def size: Int = if (isLeaf) 1 else left.size + right.size + 1

  def next(vector: FVector): Node = vector.get(index) match {
      case None => if (defaultLeft) left else right
      case Some(v) => if (v < value) left else right
    }

  def findLeaf(vector: FVector): Node = {
    var node = this
    while (!node.isLeaf) node = node.next(vector)
    node
  }

  def depth: Int = if (isLeaf) 0 else 1 + Math.max(left.depth, right.depth)

  def computeMeans(): Double = {
    val m = if (isLeaf) value else (left.computeMeans() + right.computeMeans()) / sumHess
    setMean(m)
    sumHess * m
  }

}

case object NilNode extends Node(null, null) {
  override val defaultLeft: Boolean = true
  override val value: Double =  Double.NaN
  override val index: Int = -1
  override def id: Int = -1
  override val mean: Double =  Double.NaN
  override val sumHess: Double =  Double.NaN
  override def setMean(v: Double): Unit = throw new IllegalAccessException
  override def serialize(fullTree: Boolean): Array[Byte] = throw new IllegalAccessException
}

abstract class NodeFactory {
   def fromRaw(rawNodes: IndexedSeq[RawNode]): Node
   def fromBytes(bytes: Array[Byte]): Node
}
