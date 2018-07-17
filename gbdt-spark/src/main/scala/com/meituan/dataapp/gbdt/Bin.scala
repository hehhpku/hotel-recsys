package com.meituan.dataapp.gbdt

case class Bin(
  center: Double,
  weight: Double,
  sum: Double,
  squaredSum: Double,
  aux: Double) {
  override def toString(): String = {
    center + " " + weight + " " + sum + " " + squaredSum + " " + aux
  }
}

object Bin {
  def merge(b1: Bin, b2: Bin): Bin = {
    val weight = b1.weight + b2.weight
    val center = {
      if (b1.center == b2.center) {
        b1.center
      } else {
        b1.center * (b1.weight/weight) + b2.center * (b2.weight/weight)
      }
    }
    val sum = b1.sum + b2.sum
    val squaredSum = b1.squaredSum + b2.squaredSum
    val aux = b1.aux + b2.aux

    Bin(center, weight, sum, squaredSum, aux)
  }
}
