package ai.bleckwen.xgboost

abstract class Node(val left: Node, val right: Node) extends Serializable {
  def defaultLeft: Boolean
  def value: Double
  def index: Int
  def mean: Double
  def hess: Double
  def setMean(mean: Double)
  def serialize(fullTree: Boolean = false): Array[Byte]

  lazy val isLeaf: Boolean = left == NilNode

  def size: Int = if (isLeaf) 1 else left.size + right.size + 1

  def next(vector: FVector): Node = vector.get(index) match {
      case None => if (defaultLeft) left else right
      case Some(value) => if (value < value) left else right
    }

  def findLeaf(vector: FVector): Node = {
    var node = this
    while (!node.isLeaf) node = node.next(vector)
    node
  }

  def depth: Int = if (isLeaf) 0 else 1 + Math.max(left.depth, right.depth)

  def computeMean(): Double = {
    val mean = if (isLeaf) value else (left.computeMean + right.computeMean) / hess
    setMean(mean)
    hess * mean
  }

}

case object NilNode extends Node(null, null) {
  override val defaultLeft: Boolean = true
  override val value: Double =  Double.NaN
  override val index: Int = -1
  override val mean: Double =  Double.NaN
  override val hess: Double =  Double.NaN
  override def setMean(meanValue: Double): Unit = ???
  override def serialize(fullTree: Boolean): Array[Byte] = ???
}

abstract class NodeFactory{
   def fromRaw(rawNodes: IndexedSeq[RawNode]): Node
   def fromBytes(bytes: Array[Byte]): Node
}