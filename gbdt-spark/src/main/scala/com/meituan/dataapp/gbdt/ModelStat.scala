package com.meituan.dataapp.gbdt

class ModelStat (
    val dimensions: Int
    ) {
  val selectedTimes: Array[Int] = Array.ofDim[Int](dimensions)
  val weight: Array[Double] = Array.ofDim[Double](dimensions)

  override def toString(): String = {
    val x = (0 until dimensions) map (i => (i, selectedTimes(i), weight(i))) sortBy (k => k._3)
    x map (k => k._1 + " " + k._2 + " " + k._3 + "\n") reduce ((a, b) => a + b)
  }
  
  def add(idx: Int, w: Double) = {
    selectedTimes(idx) += 1
    weight(idx) += w
  }
  
  def merge(stat : ModelStat): Boolean = {
    if (dimensions != stat.dimensions) {
      false
    } else {
      for (i <- 0 until dimensions) {
        selectedTimes(i) += stat.selectedTimes(i)
        weight(i) += stat.weight(i)
      }
      true
    }
  }
}
