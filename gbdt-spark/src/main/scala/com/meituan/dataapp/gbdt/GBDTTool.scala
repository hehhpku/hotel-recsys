package com.meituan.dataapp.gbdt

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.Logging
import com.meituan.dataapp.gbdt._

import scala.collection.mutable.ListBuffer
import scala.util.Random

case class ToolConfig(
  val numberOfFeature: Int = -1,
  val maxDepth: Int = -1,
  val iterations: Int = -1,
  val shrinkage: Double = 0,
  val featureSampleRatio: Double = 0,
  val dataSampleRatio: Double = 0,
  val minLeafSize: Int = 0,
  val enableInitialGuess: Boolean = false,
  val debug: Boolean = false,
  val loss: String = "SquaredError",
  val trainFile: String = "",
  val testFile: String = "",
  val maxBins: Int = 0,
  val modelPath: String = "",
  val hasInitValue: Boolean = false
)

object GBDTTool extends Serializable with Logging {

  def parseCli(args: Array[String]): ToolConfig = {
    var numberOfFeature: Int = -1
    var maxDepth: Int = -1
    var iterations: Int = -1
    var shrinkage: Double = 0
    var featureSampleRatio: Double = 0
    var dataSampleRatio: Double = 0
    var minLeafSize: Int = 0
    var enableInitialGuess: Boolean = false
    var debug: Boolean = false
    var loss: String = "SquaredError"
    var trainFile: String = ""
    var testFile: String = ""
    var maxBins: Int = 0
    var modelPath: String = ""
    var hasInitValue: Boolean = false

    for (s <- args) {
      val idx = s.find("=")
      if (idx < 0) {
        logWarning("unknown parameter: " + s)
      } else {
        val k = s.substring(0, idx)
        val v = s.substring(idx + 1)
        k match {
          case "dim" => numberOfFeature = v.toInt
          case "depth" => maxDepth = v.toInt
          case "iterations" => iterations = v.toInt
          case "shrinkage" => shrinkage = v.toDouble
          case "fratio" => featureSampleRatio = v.toDouble
          case "dratio" => dataSampleRatio = v.toDouble
          case "leafsize" => minLeafSize = v.toInt
          case "init" => enableInitialGuess = v.toBoolean
          case "debug" => debug = v.toBoolean
          case "loss" => loss = v
          case "trainfile" => trainFile = v
          case "testfile" => testFile = v
          case "maxBins" => maxBins = v.toInt
          case "modelPath" => modelPath = v
          case "hasInitValue" => hasInitValue = v.toBoolean
          case _ => logWarning("unknown parameter: " + s)
        }
      }
    }

    ToolConfig(
      numberOfFeature,
      maxDepth,
      iterations,
      shrinkage,
      featureSampleRatio,
      dataSampleRatio,
      minLeafSize,
      enableInitialGuess,
      debug,
      loss,
      trainFile,
      testFile,
      maxBins,
      modelPath,
      hasInitValue
    )
  }

  def main(args: Array[String]) {
    val config = parseCli(args)
    logInfo(config.toString)

    val conf = new SparkConf().setAppName("gbdt")
    val sc = new SparkContext(conf)

    val loss = {
      if (config.loss equalsIgnoreCase "LogLikelihood") {
        new LogLikelihood()
      } else {
        new SquaredError()
      }
    }

    val isClassification = {
      if (config.loss equalsIgnoreCase "LogLikelihood") {
        true
      } else {
        false
      }
    }

    val gbdtConf = new Config(
      config.numberOfFeature,
      config.maxDepth,
      config.iterations,
      config.shrinkage,
      config.featureSampleRatio,
      config.dataSampleRatio,
      config.minLeafSize,
      config.enableInitialGuess,
      config.debug,
      loss,
      config.maxBins)

    val trainInput = sc.textFile(config.trainFile)
    val trainData = trainInput.map(DataTuple.parse2(_, config.numberOfFeature, isClassification, config.hasInitValue))
    trainData.cache

    val t = GBDT.buildGBDT(trainData, gbdtConf)
    val gbdt = t._1
    val model = sc.parallelize(List(GBDT.save(gbdt)), 1)
    model.saveAsTextFile(config.modelPath)
    
    val modelStat = sc.parallelize(List(t._2.toString), 1)
    modelStat.saveAsTextFile(config.modelPath + ".stat")

    if (!config.testFile.isEmpty()) {
      val testInput = sc.textFile(config.testFile)
      val testData = testInput.map(DataTuple.parse2(_, config.numberOfFeature, isClassification, config.hasInitValue))

      val scoreAndLabels = testData.flatMap(
        d => {
          val p = loss.f(gbdt.predict(d, config.enableInitialGuess))
          val buffer = new ListBuffer[(Double, Double)]()
          var s = 0
          while (s < d.weight) {
            buffer.append((p, d.label))
            s += 1
          }
          buffer.toList
        })

      logInfo("metrics: " + loss.metrics(scoreAndLabels))
    }

    sc.stop
  }
}
