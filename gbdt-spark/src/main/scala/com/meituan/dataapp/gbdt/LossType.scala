package com.meituan.dataapp.gbdt
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.evaluation._

import scala.math._

abstract class LossType extends Serializable {
  def optimal(data: RDD[DataTuple]): Double
  def optimal2(data: Array[Bin]): Double
  def gradient(y: Double, f: Double): Double
  def init(data: RDD[DataTuple]): Double

  def f(v: Double): Double

  def metrics(scoreAndLabels: RDD[(Double, Double)]): Double
}

class SquaredError() extends LossType {
  override def optimal(data: RDD[DataTuple]): Double = {
    val c = data.map(t => t.weight).reduce((w1, w2) => w1 + w2)
    val s = data.map(t => t.target * t.weight).reduce((w1, w2) => w1 + w2)

    s/c
  }

  override def optimal2(data: Array[Bin]): Double = {
    val c = data.filter(b => b != null).map(b => b.weight).reduce((w1, w2) => w1 + w2)
    val s = data.filter(b => b != null).map(b => b.sum).reduce((w1, w2) => w1 + w2)

    s/c
  }

  override def gradient(y: Double, f: Double): Double = {
    y-f
  }

  override def init(data: RDD[DataTuple]): Double = {
    val c = data.map(t => t.weight).reduce((w1, w2) => w1 + w2)
    val s = data.map(t => t.label * t.weight).reduce((w1, w2) => w1 + w2)

    s/c
  }

  override def f(v: Double): Double = {
    v
  }

  override def metrics(scoreAndLabels: RDD[(Double, Double)]): Double = {
    val s = scoreAndLabels.map(f => (f._1 - f._2) * (f._1 - f._2)).reduce((a, b) => a + b)
    sqrt(s / scoreAndLabels.count)
  }
}

class LogLikelihood() extends LossType {
  override def optimal(data: RDD[DataTuple]): Double = {
    val s = data.map(d => d.target * d.weight).reduce((v1, v2) => v1 + v2)
    val c = data.map(d => (2.0*abs(d.target) - d.target*d.target) * d.weight).reduce((v1, v2) => v1 + v2)
    if (c == 0) {
      0.0
    } else {
      s/c
    }
  }

  override def optimal2(data: Array[Bin]): Double = {
    val c = data.filter(b => b != null).map(b => b.aux).reduce((w1, w2) => w1 + w2)
    val s = data.filter(b => b != null).map(b => b.sum).reduce((w1, w2) => w1 + w2)

    if (c == 0) {
      0.0
    } else {
      s/c
    }
  }

  override def gradient(y: Double, f: Double): Double = {
    2.0 * y / (1.0+exp(2.0*y*f))
  }

  override def init(data: RDD[DataTuple]): Double = {
    val c = data.map(t => t.weight).reduce((w1, w2) => w1 + w2)
    val s = data.map(t => t.label * t.weight).reduce((w1, w2) => w1 + w2)

    val v = s/c
    log((1.0+v) / (1.0-v)) / 2.0
  }

  override def f(v: Double): Double = {
    1.0 / (1.0 + exp(-2.0*v))
  }

  override def metrics(scoreAndLabels: RDD[(Double, Double)]): Double = {
    val metric = new BinaryClassificationMetrics(scoreAndLabels)
    metric.areaUnderROC
  }
}
