package ai.bleckwen.xgboost

/**
 * An XGboost Decision Tree
 * Note that algorithms are independent of Nodes implementation as Node is abstract
 * @param root root node
 */
final case class DecisionTree(root: Node) {

  lazy val maxDepth: Int = root.depth

  lazy val size: Int = root.size

  def getScore(vector: FVector): Double = root.findLeaf(vector).value

  def serialize: Array[Byte] = root.serialize(fullTree = true)

  def computeApproxContrib(vector: FVector, contribs: Array[Double]): Unit = {
    var node = root
    var nodeValue = node.mean
    contribs(contribs.length -1) += nodeValue
    while (!node.isLeaf) {
      val index = node.index
      node = node.next(vector)
      val newValue = node.mean
      contribs(index) += newValue - nodeValue
      nodeValue = newValue
    }
  }

  def computeContrib(vector: FVector, contribs: Array[Double]): Unit = {
    PathElement().computeShap(vector, contribs, root)
    contribs(contribs.length -1) += root.mean
  }
}

object DecisionTree {
  def apply(factory: NodeFactory, rawNodes: IndexedSeq[RawNode]): DecisionTree = {
    val tree = DecisionTree(factory.fromRaw(rawNodes))
    tree.root.computeMeans()
    tree
  }

  def apply(factory: NodeFactory, bytes: Array[Byte]): DecisionTree = DecisionTree(factory.fromBytes(bytes))
}
