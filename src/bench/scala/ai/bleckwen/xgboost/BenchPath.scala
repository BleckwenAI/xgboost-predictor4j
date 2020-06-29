package ai.bleckwen.xgboost

import java.io.FileInputStream
import java.util.concurrent.TimeUnit

import org.apache.commons.io.IOUtils
import org.openjdk.jmh.annotations.{Benchmark, OutputTimeUnit, Scope, State}

import scala.util.Random

/*
Those benchmarks are for unit testing after refactos. Uncomment the annotations as needed
 */

class BenchPath {
  import BenchPath._

  //@Benchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def computeWeights(ctx: BenchmarkState): Unit = {
    ctx.randomPath.computeWeights()
  }

  //@Benchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def copyAll(ctx: BenchmarkState): Unit = {
    ctx.randomPath.copyAll()
  }

  //@Benchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def sumUnboundPath(ctx: BenchmarkState): Unit = {
    val p=ctx.randomPath
    p.sumUnboundPath(p)
  }

  // @Benchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def computeShap(ctx: BenchmarkState): Unit = {
    PathElement().computeShap(ctx.vector, ctx.contribs, ctx.predictor.trees(ctx.rand.nextInt(100)).root)
  }
}

object BenchPath {

  @State(Scope.Benchmark)
  class BenchmarkState {
    val rand: Random.type = Random
    private val bytes = IOUtils.toByteArray(new FileInputStream("generated.model"))
    val predictor: Predictor = Predictor(bytes)
    val vector: DenseFVector = DenseFVector((1 to predictor.params.nbFeatures).map(_ => rand.nextGaussian()).toArray)
    val contribs: Array[Double] = Array.fill[Double](predictor.params.nbFeatures + 1)(0.0)
    private val paths = predictor.trees.map(samplePath(_, rand)).toIndexedSeq
    var i = 0
    def randomPath: PathElement = {
      i = (i+1) % paths.length
      paths(i)
    }
  }

  private def samplePath(tree: DecisionTree, rand: Random) : PathElement = {
    var node = tree.root
    var path = PathElement()
    while (!node.isLeaf) {
      val child = if (rand.nextBoolean()) node.left else node.right
      path = PathElement(path, node, child,  if (rand.nextBoolean()) 1.0 else 0.0)
      path.setWeight(rand.nextGaussian())
      node = child
    }
    path
  }
}
