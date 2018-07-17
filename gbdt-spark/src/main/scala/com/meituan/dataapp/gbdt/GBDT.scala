package com.meituan.dataapp.gbdt

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._
import org.apache.spark.Logging

import scala.collection.mutable.ListBuffer

class GBDT(
  val trees: Array[RegressionTree],
  val bias: Double,
  val shrinkage: Double
) extends Serializable {
  def predict(d: DataTuple, enableInitialGuess: Boolean = false): Double = {
    val v0 = {
      if (enableInitialGuess) {
        d.initValue
      } else {
        bias
      }
    }
    trees.foldLeft(v0)((v, t) => v + t.predict(d)*shrinkage)
  }
}

object GBDT extends Serializable with Logging {
  def buildGBDT(data: RDD[DataTuple], conf: Config): (GBDT, ModelStat) = {
    val trees = new Array[RegressionTree](conf.iterations)

    val modelStat = new ModelStat(conf.numberOfFeature)

    val bias = {
      if (conf.enableInitialGuess) {
        0
      } else {
        conf.loss.init(data)
      }
    }
    for (i <- 0 until conf.iterations) {
      logInfo("Iteration: " + i)
      val samples = {
        if (conf.dataSampleRatio < 1) {
          data.sample(false, conf.dataSampleRatio, System.currentTimeMillis())
        } else {
          data
        }
      }

      val fitData = samples.map(d => {
        val v0 = {
          if (conf.enableInitialGuess) {
            d.initValue
          } else {
            bias
          }
        }
        val p = GBDT.predict(d, trees, v0, conf.shrinkage, i)
        d.target = conf.loss.gradient(d.label, p)
        d
      }).cache.persist

      if (conf.debug) {
        val scoreAndLabels = samples.flatMap(d => {
          val v0 = {
            if (conf.enableInitialGuess) {
              d.initValue
            } else {
              bias
            }
          }
          val p = GBDT.predict(d, trees, v0, conf.shrinkage, i)
          val buffer = new ListBuffer[(Double, Double)]()
          var s = 0
          while (s < d.weight) {
            buffer.append((p, d.label))
            s += 1
          }
          buffer.toList
        })

        logInfo("metrics: " + conf.loss.metrics(scoreAndLabels))
      }

      val t = RegressionTree.buildRegressionTree(fitData, conf)
      trees(i) = t._1
      modelStat.merge(t._2)

      fitData.unpersist(false)
    }
    (new GBDT(trees, bias, conf.shrinkage), modelStat)
  }

  private def predict(d: DataTuple,
    trees: Array[RegressionTree],
    initVal: Double,
    shrinkage: Double,
    iterations: Int): Double = {
    var p = initVal
    for (i <- 0 until iterations) {
      p += trees(i).predict(d)*shrinkage
    }
    p
  }

  def save(t: GBDT): String = {
    val builder = new StringBuilder()
    builder.append(t.shrinkage)
    builder.append("\n;\n")
    builder.append(t.bias)
    for (r <- t.trees) {
      builder.append("\n;\n")
      builder.append(RegressionTree.save(r))
    }
    builder.toString
  }

  def load(s: String): GBDT = {
    val tokens = s.split("\n;\n")
    val iterations = tokens.length - 2
    val trees = new Array[RegressionTree](iterations)
    val shrinkage = tokens(0).toDouble
    val bias = tokens(1).toDouble
    for (i <- 0 until iterations) {
      trees(i) = RegressionTree.load(tokens(i+2))
    }

    new GBDT(trees, bias, shrinkage)
  }
}
