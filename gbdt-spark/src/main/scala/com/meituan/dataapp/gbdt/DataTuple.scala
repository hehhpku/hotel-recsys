package com.meituan.dataapp.gbdt

class DataTuple(
    val initValue: Double,
    val label: Double,
    var target: Double,
    val weight: Double,
    val feature: Array[Double]) extends Serializable {
  override def toString(): String = {
    val builder = new StringBuilder()
    builder.append(initValue)
    builder.append(" ")
    builder.append(label)
    builder.append(" ")
    builder.append(weight)
    for (idx <- 0 until feature.length) {
      builder.append(" ")
      builder.append(idx + ":" + feature(idx))
    }
    builder.toString
  }
}

object DataTuple extends Serializable {
  val UNKNOWN_VALUE = Double.MinValue
  def parse(s: String, n: Int, classification: Boolean, hasInitValue: Boolean): DataTuple = {
    val tokens = s.split(" ")
    val initValue = {
      if (hasInitValue) {
        tokens(0).toDouble
      } else {
        0.0
      }
    }
    val offset = {
      if (hasInitValue) {
        1
      } else {
        0
      }
    }
    val label = {
      val l = tokens(offset).toDouble
      if (classification) {
        if (l > 0) {
          1
        } else {
          -1
        }
      } else {
        l
      }
    }
    val weight = tokens(offset + 1).toDouble
    val feature = Array.fill(n)(UNKNOWN_VALUE)

    for (i <- (offset + 2) until tokens.length) {
      val kv = tokens(i).split(":")
      feature(kv(0).toInt) = kv(1).toDouble
    }

    new DataTuple(initValue, label, 0.0, weight, feature)
  }
  
  
  def parse2(s: String, n: Int, classification: Boolean, hasInitValue: Boolean): DataTuple = {
    val tokens = s.split(" ")
    val initValue = {
      if (hasInitValue) {
        tokens(0).toDouble
      } else {
        0.0
      }
    }
    val offset = {
      if (hasInitValue) {
        1
      } else {
        0
      }
    }
    val label = {
      val l = tokens(offset + 1).toDouble
      if (classification) {
        if (l > 0) {
          1
        } else {
          -1
        }
      } else {
        l
      }
    }
    val weight = tokens(offset).toDouble
    val feature = Array.fill(n)(UNKNOWN_VALUE)

    for (i <- (offset + 2) until tokens.length) {
      val kv = tokens(i).split(":")
      feature(kv(0).toInt) = kv(1).toDouble
    }

    new DataTuple(initValue, label, 0.0, weight, feature)
  }
}
