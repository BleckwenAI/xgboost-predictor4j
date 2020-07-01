package ai.bleckwen.xgboost

import org.scalatest.{Matchers, WordSpec}

class DecisionTreeSpec extends WordSpec with Matchers with DoubleTolerant {
  private val nodefactory = NodeFactoryImpl
  private val rawNodes = List(
    RawNode(0, 1, 2, isLeaf = false, 1.0f, defaultLeft = true, 1, 0.5f),
    RawNode(1, 3, 4, isLeaf = false, 2.0f, defaultLeft = false, 2, 0.3f),
    RawNode(2, -1, -1, isLeaf = true, 3.0f, defaultLeft = false, 3, 0.2f),
    RawNode(3, -1, -1, isLeaf = true, 4.0f, defaultLeft = true, -1, 0.1f),
    RawNode(4, -1, -1, isLeaf = true, 5.0f, defaultLeft = false, -1, 0.6f)
  ).toArray
  private val nbFeatures = 3

  "DecisionTree" should {

    "load tree from RawNodes and compute means" in {
      val tree = DecisionTree(nodefactory, rawNodes)
      tree.root.mean shouldEqual 8.0
      tree.maxDepth shouldBe 2
      tree.size shouldBe rawNodes.length
    }

    "implement serialize()" in {
      val tree = DecisionTree(nodefactory, rawNodes)
      val deser = DecisionTree(nodefactory, tree.serialize)
      deser.maxDepth shouldBe tree.maxDepth
    }

    "compute score and contribs" in {
      val tree = DecisionTree(nodefactory, rawNodes)
      val vector = DenseFVector(Array(1.0, 0.0, 0.0))
      val contribs = Array.fill[Double](nbFeatures + 1)(0.0)
      tree.getScore(vector) shouldBe 5.0
      tree.computeApproxContrib(vector, contribs)
      contribs shouldEqual Array(0.0, 3.333333, -6.333333, 8.0)
    }
  }
}

