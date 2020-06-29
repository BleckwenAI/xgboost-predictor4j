package ai.bleckwen.xgboost

import java.util.concurrent.TimeUnit

import ml.dmlc.xgboost4j.java.{Booster, DMatrix, XGBoost}
import org.openjdk.jmh.annotations.{Benchmark, OutputTimeUnit, Scope, State}

import scala.util.Random

class BenchXgboost4j {
  import BenchXgboost4j._

  @Benchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def predict(ctx: BenchmarkState): Unit = {
    val dmatrix = ctx.samples(ctx.rand.nextInt(100))
    ctx.booster.predict(dmatrix)
  }

  @Benchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def predictContrib(ctx: BenchmarkState): Unit = {
    val dmatrix = ctx.samples(ctx.rand.nextInt(100))
    ctx.booster.predictContrib(dmatrix, 0)
  }
}


object BenchXgboost4j {

  @State(Scope.Benchmark)
  class BenchmarkState {
    val booster: Booster = XGBoost.loadModel("generated.model")
    private val nbfeatures = 126
    val rand: Random.type = Random
    val samples: IndexedSeq[DMatrix] = (1 to 100).map(_ => new DMatrix((1 to nbfeatures).map(_ => rand.nextGaussian().toFloat).toArray, 1, nbfeatures))
  }
}
