xgboost-predictor4j
===================

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![build](https://github.com/BleckwenAI/xgboost-predictor4j/workflows/build/badge.svg)](https://github.com/BleckwenAI/xgboost-predictor4j/actions)
![Coverage](https://img.shields.io/badge/coverage-85%25-<COLOR>.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ai.bleckwen/xgboost-predictor4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ai.bleckwen/xgboost-predictor4j)
[![Documentation](https://img.shields.io/badge/wiki-ok-<COLOR>.svg)](https://github.com/BleckwenAI/xgboost-predictor4j/wiki)

[Bleckwen](https://bleckwen.ai/) JVM implementation of [XGBoost](https://github.com/dmlc/xgboost/) Predictor

**Features**
* Faster than XGboost4j especially on distributed frameworks like Flink or Spark
* No dependency (no need to install `libgomp`)
* Designed for streaming (on-the-fly prediction)
* Scala and Java APIs with flexible input (Array or FVector)
* Compatible with XGboost models 0.90 and 1.0.0 
* Support of ML interpretability with fast algorithm (`predictApproxContrib`) and slower SHAP algorithm (`predictContrib`)

**Limitations**
* Only binary classification (binary:logistic) is supported in this release
* predictContrib() use SHAP algorithm described in this [paper](https://arxiv.org/pdf/1802.03888.pdf) but does not check for duplicate indexes (`rewind` is not implemented).
The impact is negligeable as it happens in very rare situation (a comparison with XGBoots4J performed on 1_000_000 random records did not show any discrepancy)

**Release History**
* 1.0 06/07/2020 first version
* 1.1 12/04/2021 compatibility with 1.4.0 binary files

**Integration**

* Maven 
```xml
<dependency>
  <groupId>ai.bleckwen</groupId>
  <artifactId>xgboost-predictor4j</artifactId>
  <version>1.0</version>
</dependency>
```
* SBT
```
libraryDependencies += "ai.bleckwen" % "xgboost-predictor4j" % "1.0"
```

The package was build and published wih **Scala 2.11.12** but you can rebuild it with Scala 2.12 by using Maven profile `scala12` or by using the Makefile goal. 

**Using Predictor in Scala**

```java
  val bytes = org.apache.commons.io.IOUtils.toByteArray(this.getClass.getResourceAsStream("/path_to.model"))
  val predictor = Predictor(bytes)
  val denseArray = Array(0.23, 0.0, 1.0, 0.5)
  val score = predictor.predict(denseArray).head
```

**Using Predictor in Java**

```java
   byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(this.getClass().getResourceAsStream("/path_to.model"));
   Predictor predictor = (new PredictorBuilder()).build(bytes) ;
   double[] denseArray = {0, 0, 32, 0, 0, 16, -8, 0, 0, 0};
   double score = predictor.predict(denseArray)[0];
```

**Benchmarks**

See [BENCH.md](https://github.com/BleckwenAI/xgboost-predictor4j/blob/master/BENCH.md)


