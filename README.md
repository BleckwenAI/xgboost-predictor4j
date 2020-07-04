xgboost-predictor4j
===================

![build](https://github.com/BleckwenAI/xgboost-predictor4j/workflows/build/badge.svg)


Pure JVM implementation of [XGBoost](https://github.com/dmlc/xgboost/) predictor in Scala

**Features**
* Fast (faster than XGboost4j)
* No dependency at all (no need to install `libgomp`)
* Designed for streaming (on-the-fly prediction)
* Scala and Java APIs with flexible input (Array or FVector)
* Compatible with XGboost models 0.90 and 1.0.0 
* Support of ML interpretability with fast Means algorithm (`predictApproxContrib`) and slower SHAP algorithm (`predictContrib`)

**Limitations**
* Only binary classification (binary:logistic) is supported in this release
* predictContrib() use SHAP algorithm described in this [paper](https://arxiv.org/pdf/1802.03888.pdf) but does not check for duplicate indexes (`rewind` is not implemented).
The impact is negligeable as it happens in very rare situation (a comparison with XGBoots4J performed on 1_000_000 records did not raise any discrepancy)

**Integration**

The package is not yet available on Maven Central.

* With Maven 
```xml
<dependency>
  <groupId>ai.bleckwen</groupId>
  <artifactId>xgboost-predictor4j</artifactId>
  <version>0.1</version>
</dependency>
```
* With SBT
```
libraryDependencies += "ai.bleckwen" % "xgboost-predictor4j" % "0.1"
```

The package was build and published wih **Scala 2.11.12** but you can rebuild it with Scala 2.12 by using Maven profile `scala12` or by using the Makefile goal. 

**Using Predictor in Scala**

```java
  val bytes = org.apache.commons.io.IOUtils.toByteArray(this.getClass.getResourceAsStream("my model path"))
  val predictor = Predictor(bytes)
  val denseArray = Array(0.23, 0.0, 1.0, 0.5)
  val score = predictor.predict(denseArray).head
```

**Using Predictor in Java**

```java
   byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(this.getClass().getResourceAsStream("my model path"));
   Predictor predictor = (new PredictorBuilder()).build(bytes) ;
   double[] denseArray = {0, 0, 32, 0, 0, 16, -8, 0, 0, 0};
   double[] prediction = predictor.predict(denseArray);
```

**Benchmarks**

The predictions are performed on the generated XGboost model `generated.model` (having 126 features and 1000 trees)
  
The figures below were done with a single thread on a Intel(R) Core(TM) i5-6400
```
Benchmark                           Mode  Cnt   Score    Error   Units
BenchPredict.predictContrib        thrpt    5   6.300 ±  0.006  ops/ms
BenchPredict.predictContribApprox  thrpt    5  87.402 ± 11.126  ops/ms
BenchPredict.predict               thrpt    5  98.762 ±  1.583  ops/ms
BenchXgboost4j.predict             thrpt    5  68.195 ±  4.002  ops/ms
BenchXgboost4j.predictContrib      thrpt    5   7.269 ±  1.721  ops/ms
```

Note that Xgboost4j figures are really fluctuent and seem to depend a lot upon system status (because of JNI?)

**TO DO**
* Multiclass support

