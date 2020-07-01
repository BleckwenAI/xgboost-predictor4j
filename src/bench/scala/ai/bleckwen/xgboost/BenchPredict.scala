package ai.bleckwen.xgboost

import java.io.FileInputStream
import java.util.concurrent.TimeUnit

import org.apache.commons.io.IOUtils
import org.openjdk.jmh.annotations.{Benchmark, OutputTimeUnit, Scope, State}

import scala.util.Random

class BenchPredict {
  import BenchPredict._

  @Benchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def predict(ctx: BenchmarkState): Unit = {
    val fvec = ctx.samples(ctx.rand.nextInt(100))
    ctx.predictor.predict(fvec)
  }

  @Benchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def predictContribApprox(ctx: BenchmarkState): Unit = {
    val fvec = ctx.samples(ctx.rand.nextInt(100))
    ctx.predictor.predictApproxContrib(fvec)
  }

  @Benchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def predictContrib(ctx: BenchmarkState): Unit = {
    val fvec = ctx.samples(ctx.rand.nextInt(100))
    ctx.predictor.predictContrib(fvec)
  }


}

object BenchPredict {

  @State(Scope.Benchmark)
  class BenchmarkState {
    private val bytes = IOUtils.toByteArray(new FileInputStream("generated.model"))
    val predictor: Predictor = Predictor(bytes)
    private val nbfeatures = predictor.params.nbFeatures
    val rand: Random.type = Random
    val samples: IndexedSeq[DenseFVector] = (1 to 100).map(_ => DenseFVector((1 to nbfeatures).map(_ => rand.nextGaussian()).toArray))
   }
}
