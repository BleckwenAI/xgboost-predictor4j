package ai.bleckwen.xgboost

import org.scalatest.{Matchers, WordSpec}

class PathElementSpec extends WordSpec with Matchers with DoubleTolerant {

  val tree: DecisionTree = DecisionTree(NodeFactoryImpl, Array(
    RawNode(0, 1, 2, false, -9.536743E-7f, true, 0, 402.75f),
    RawNode(1, 3, 4, false, -9.536743E-7f, true, 4, 382.0f),
    RawNode(2, -1, -1, true, -0.4344814f, false, 0, 20.75f),
    RawNode(3, -1, -1, true, -0.031157183f, false, 0, 336.0f),
    RawNode(4, -1, -1, true, 0.23616959f, false, 0, 46.0f)))

  "PathElement" should {

    "set depth on creation" in {
      samplePath().depth shouldBe 3.0
    }

    "compute correct weights" in {
      val path = samplePath()
      path.parent.parent.weight shouldEqual 1.0
      path.parent.computeWeights()
      path.parent.weight shouldEqual 0.50
      path.parent.parent.weight shouldEqual 0.474239602731
      path.computeWeights()
      path.weight shouldEqual 0.33333333333
      path.parent.weight shouldEqual 0.3046767262158
      path.parent.parent.weight shouldEqual 0.278088144009
    }

    "copy path and preserve depth and weights" in {
      val path = samplePath()
      path.parent.computeWeights()
      path.computeWeights()
      val duppath = path.copyAll
      duppath.depth shouldEqual path.depth
      duppath.weight shouldEqual path.weight
      duppath.parent.parent.weight shouldEqual path.parent.parent.weight
      duppath.computeWeights()
      duppath.weight should not equal path.weight
    }


    "compute sumUnboundpath" in {
      val path = samplePath()
      path.parent.computeWeights()
      path.computeWeights()
      path.sumUnboundPath(path) shouldEqual 0.97423960
    }

    "compute SHAP values for a path element" in {
      val path = samplePath()
      path.parent.computeWeights()
      val contribs = Array.fill[Double](6)(0.0)
      val expected = Array(-0.00150859, 0.0, 0.0, 0.0, -0.00365526, 0.0)
      val testVector = DenseFVector(Array(0.0, 1.0, 1.0, 0.0, 0.0))
      path.computeShap(testVector, contribs, tree.root.left.left)
      contribs shouldEqual expected
    }

  }
  private def samplePath(): PathElement = {
    val path = PathElement(PathElement(), tree.root, tree.root.left, 1.0)
    PathElement(path, tree.root.left, tree.root.left.left, 1.0)
  }
}
