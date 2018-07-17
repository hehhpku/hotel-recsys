package com.meituan.dataapp.metrics

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.Logging
import scala.Iterable

object MAP extends Serializable with Logging {

  def getLabel(s: ScoredSample, label: String): Int = {
    label match {
      case "click" => s.clicked
      case "order" => s.ordered
      case "pay" => s.paid
    }
  }

  def calSessionMetric(samples: Iterable[ScoredSample], label: String): Double = {
    val x = samples.toList.sortBy(-_.score)
    var s = 0.0
    var p = 0.0
    var r = 0.0
    for (z <- x) {
      s += 1.0
      val l = getLabel(z, label)
      if (l > 0) {
        p += 1.0
        r += p/s
      }
    }
    if (p == 0.0) {
      0.0
    } else {
      r/p
    }
  }

  def main(args: Array[String]) {
    val inputPath = args(0)
    val label = args(1)

    val conf = new SparkConf().setAppName("map-eval")
    val sc = new SparkContext(conf)

    val evalInput = sc.textFile(inputPath)
    val evalData = evalInput.map(ScoredSample.parse(_)).map(s => (s.sessionId, s))

    val m = evalData.groupByKey.map(g => (1, calSessionMetric(g._2, label))).reduce((a,b) => ((a._1 + b._1, a._2 + b._2)))

    logInfo("MAP = " + m._2 + "/" + m._1 + " = " + (m._2 / m._1))

    sc.stop
  }
}


case class ScoredSample(
                         val sessionId: String,
                         val clicked: Int,
                         val ordered: Int,
                         val paid: Int,
                         val score: Double)

object ScoredSample {
  def parse(l: String): ScoredSample = {
    val tokens = l.split("\t")
    val sid = tokens(0)
    val clicked = tokens(1).toInt
    val ordered = tokens(2).toInt
    val paid = tokens(3).toInt
    val score = tokens(4).toDouble
    ScoredSample(sid, clicked, ordered, paid, score)
  }
}
