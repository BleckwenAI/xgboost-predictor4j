package ai.bleckwen.xgboost

import scala.annotation.tailrec

/**
 * Element of a unique Path in SHAP algorithm https://arxiv.org/pdf/1802.03888.pdf
 * @param parent parent element (NilPath in case of root path)
 */
abstract class PathElement(val parent: PathElement) {

  def p0: Double
  def p1: Double
  def index: Int
  def depth: Double  // we use a double to avoid conversion during calculus
  def weight: Double
  def setWeight(v: Double)
  def copyAll: PathElement

  lazy val hasParent: Boolean = parent != NilPath

  def computeShap(vector: FVector, contribs: Array[Double], node: Node): Unit = {
    if (hasParent)
      computeWeights()
    if (node.isLeaf) {
      var el = this
      while (el.hasParent) {
        contribs(el.index) += (el.p1 - el.p0) * node.value * sumUnboundPath(el)
        el = el.parent
      }
    } else {
      val hotChild = node.next(vector)
      val coldChild = if (hotChild == node.left) node.right else node.left
      PathElement(this.copyAll, node, hotChild, 1.0).computeShap(vector, contribs, hotChild)
      PathElement(this.copyAll, node, coldChild, 0.0).computeShap(vector, contribs, coldChild)
    }
  }

  def computeWeights(): Unit = {
    var el = this
    while (el.hasParent) {
      val elParent = el.parent
      el.setWeight(el.weight + p1 * elParent.weight * (el.depth - 1.0) / depth)
      elParent.setWeight(p0 * elParent.weight * (depth + 1.0 - el.depth) / depth)
      el = elParent
    }
  }

  def sumUnboundPath(target: PathElement): Double = {
    var total = 0.0
    var nextOnePortion = weight
    var el = this
    while (el.hasParent) {
      el = el.parent
      if (target.p1 != 0) {
        val tmp = nextOnePortion * target.p1 * depth / (target.p1 * el.depth)
        total += tmp
        if (el.hasParent)
          nextOnePortion = el.weight - tmp * target.p0 * (depth - el.depth) / depth
      } else if (target.p0 != 0) {
        total += el.weight * depth / (target.p0 * (depth - el.depth))
      }
    }
    total
  }

}

case object NilPath extends PathElement(null) {
  override def p0: Double = Double.NaN
  override def p1: Double = Double.NaN
  override def depth: Double = -1
  override def weight: Double = Double.NaN
  override def setWeight(v: Double): Unit = throw new IllegalAccessException
  override def copyAll: PathElement = this
  override def index: Int = -1
}

object PathElement {
  def apply(parent: PathElement, parentNode: Node, childNode: Node, oneFraction: Double): PathElement =
    PathElementImpl(parent, childNode.sumHess / parentNode.sumHess, oneFraction, parentNode.index, parent.depth + 1, 0.0)

  def apply(): PathElement = PathElementImpl(NilPath, 1.0, 1.0, -1, 1, 1.0)
}

final case class PathElementImpl(override val parent: PathElement,
                           p0: Double,
                           p1: Double,
                           index: Int,
                           depth: Double,
                           var weight: Double)
  extends PathElement(parent) {

  override def setWeight(v: Double): Unit = this.weight = v

  override def toString: String = f"PathElementImpl($p0,$p1,$index,$depth,$weight)"

  override def copyAll: PathElement = copy(parent = parent.copyAll)
}
