package com.meituan.dataapp.gbdt

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.Logging
import com.meituan.dataapp.gbdt._

import scala.collection.mutable.ListBuffer

case class EvaluateConfig (
  val modelPath: String,
  val testPath: String,
  val outputPath: String,
  val loss: String,
  val numberOfFeature: Int,
  val enableInitialGuess: Boolean,
  val hasInitValue: Boolean
)

object GBDTEvaluate extends Serializable with Logging {
  def parseCli(args: Array[String]): EvaluateConfig = {
    var modelPath: String = ""
    var testPath: String = ""
    var outputPath: String = ""
    var loss: String = ""
    var numberOfFeature: Int = 0
    var enableInitialGuess: Boolean = false
    var hasInitValue: Boolean = false
    for (arg <- args) {
      val kv = arg.split("=")
      kv(0) match {
        case "modelPath" => modelPath = kv(1)
        case "testPath" => testPath = kv(1)
        case "outputPath" => outputPath = kv(1)
        case "loss" => loss = kv(1)
        case "numberOfFeature" => numberOfFeature =kv(1).toInt
        case "enableInitialGuess" => enableInitialGuess = kv(1).toBoolean
        case "hasInitValue" => hasInitValue = kv(1).toBoolean
      }
    }

    EvaluateConfig(
      modelPath,
      testPath,
      outputPath,
      loss,
      numberOfFeature,
      enableInitialGuess,
      hasInitValue)
  }

  def main(args: Array[String]) {
    val evalConfig = parseCli(args)

    val conf = new SparkConf().setAppName("gbdt-eval")
    val sc = new SparkContext(conf)

    val loss = {
      if (evalConfig.loss equalsIgnoreCase "LogLikelihood") {
        new LogLikelihood()
      } else {
        new SquaredError()
      }
    }
    
    val isClassification = {
      if (evalConfig.loss equalsIgnoreCase "LogLikelihood") {
        true
      } else {
        false
      }
    }
    
    val content = sc.textFile(evalConfig.modelPath).collect.mkString("\n")
    val gbdt = GBDT.load(content)

    val testInput = sc.textFile(evalConfig.testPath)
    val testData = testInput.map(DataTuple.parse2(_, evalConfig.numberOfFeature, isClassification, evalConfig.hasInitValue))

    val scoreAndLabels = testData.flatMap(
      d => {
        val p = loss.f(gbdt.predict(d, evalConfig.enableInitialGuess))
        val buffer = new ListBuffer[(Double, Double)]()
        var s = 0
        while (s < d.weight) {
          buffer.append((p, d.label))
          s += 1
        }
        buffer.toList
      })

    logInfo("metrics: " + loss.metrics(scoreAndLabels))
    
    val predict = testData.map(
        d => {
          val p = loss.f(gbdt.predict(d, evalConfig.enableInitialGuess))
          p + ": " + d.toString()
        })
    predict.saveAsTextFile(evalConfig.outputPath)

    sc.stop
  }
}
