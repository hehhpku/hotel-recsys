package com.meituan.dataapp.gbdt

import java.util.PriorityQueue
import java.lang.Comparable

case class Histogram(histo: Array[Bin]) {
  override def toString(): String = {
    histo.mkString("[", ",", "]")
  }
  def getSize(): Int = {
    histo.filter(b => b != null).length
  }
}

object Histogram {
  def merge(h1: Histogram, h2: Histogram, s:Int): Histogram = {
    val v = Array.ofDim[Bin](h1.histo.length + h2.histo.length)
    var i = 0
    var j = 0
    var idx = 0
    while (i < h1.histo.length && h1.histo(i) != null
      && j < h2.histo.length && h2.histo(j) != null) {
      if (RegressionTree.almostEqual(h1.histo(i).center, h2.histo(j).center)) {
        v(idx) = Bin.merge(h1.histo(i), h2.histo(j))
        i += 1
        j += 1
      } else if (h1.histo(i).center < h2.histo(j).center) {
        v(idx) = h1.histo(i)
        i += 1
      } else {
        v(idx) = h2.histo(j)
        j += 1
      }
      idx += 1
    }
    while (i < h1.histo.length && h1.histo(i) != null) {
      v(idx) = h1.histo(i)
      i += 1
      idx += 1
    }
    while (j < h2.histo.length && h2.histo(j) != null) {
      v(idx) = h2.histo(j)
      j += 1
      idx += 1
    }

    if (idx <= s) {
      val o = Array.ofDim[Bin](s)
      for (t <- 0 until idx) {
        o(t) = v(t)
      }
      Histogram(o)
    } else {
      shrink(Histogram(v), s)
    }
  }

  class BinDiff(var left: Int, val idx: Int, var right: Int, var diff: Double) extends Comparable[BinDiff] {
    override def compareTo(that: BinDiff): Int = {
      if (this.diff < that.diff) {
        -1
      } else if (this.diff > that.diff) {
        1
      } else {
        0
      }
    }
  }

  private def shrink(h: Histogram, s: Int): Histogram = {
    val len = h.getSize
    val diffs = Array.ofDim[BinDiff](len-1)
    val pq = new PriorityQueue[BinDiff](len-1)
    for (i <- 1 until len) {
      diffs(i-1) = new BinDiff(i-2, i-1, i, h.histo(i).center - h.histo(i-1).center)
      pq.add(diffs(i-1))
    }

    var l = len
    while (l > s) {
      val diff = pq.poll()

      val x = Bin.merge(h.histo(diff.idx), h.histo(diff.right))
      h.histo(diff.idx) = null
      h.histo(diff.right) = x

      if (diff.right < (len-1)) {
        val rr = diffs(diff.right).right
        val d = h.histo(rr).center - h.histo(diff.right).center
        diffs(diff.right).diff = d
        diffs(diff.right).left = diff.left
        pq.remove(diffs(diff.right))
        pq.add(diffs(diff.right))
      }

      if (diff.left >= 0) {
        val d = h.histo(diff.right).center - h.histo(diff.left).center
        diffs(diff.left).right = diff.right
        diffs(diff.left).diff = d
        pq.remove(diffs(diff.left))
        pq.add(diffs(diff.left))
      }

      l -= 1
    }

    val o = Array.ofDim[Bin](s)
    var idx = 0
    for (b <- h.histo) {
      if (b != null) {
        o(idx) =b
        idx += 1
      }
    }

    Histogram(o)
  }

  def findBestSplit(h: Array[Histogram]): (Int, Double, Double, Double) = {
    var bestIdx = 0
    var bestSplit = 0.0
    var bestImpurity = Double.MaxValue
    var bestGain = 0.0
    for (i <- 0 until h.length) {
      val r = getImpurity(h(i))
      if (r._2  < bestImpurity) {
        bestSplit = r._1
        bestImpurity = r._2
        bestIdx = i
        bestGain = r._3
      }
    }

    (bestIdx, bestSplit, bestImpurity, bestGain)
  }

  def getImpurity(h: Histogram): (Double, Double, Double) = {
    var len = h.getSize

    if (len <= 1) {
      return (0.0, Double.MaxValue, 0.0)
    }

    val fitness0 = {
      if (h.histo(0).center == DataTuple.UNKNOWN_VALUE) {
        RegressionTree.calcVar(h.histo(0).sum, h.histo(0).squaredSum, h.histo(0).weight)
      } else {
        0.0
      }
    }

    val beginIdx = {
      if (h.histo(0).center == DataTuple.UNKNOWN_VALUE) {
        1
      } else {
        0
      }
    }

    var rs = 0.0
    var rss = 0.0
    var rc = 0.0
    for (i <- 0 until len) {
      rs += h.histo(i).sum
      rss += h.histo(i).squaredSum
      rc += h.histo(i).weight
    }
    
    val fitnessOrigin = RegressionTree.calcVar(rs, rss, rc);
    
    if (beginIdx == 1) {
      rs -= h.histo(0).sum
      rss -= h.histo(0).squaredSum
      rc -= h.histo(0).weight
    }

    var ls = 0.0
    var lss = 0.0
    var lc = 0.0

    var bestSplit = 0.0
    var bestImpurity = Double.MaxValue
    for (i <- beginIdx until (len -1)) {
      ls += h.histo(i).sum
      lss += h.histo(i).squaredSum
      lc += h.histo(i).weight

      rs -= h.histo(i).sum
      rss -= h.histo(i).squaredSum
      rc -= h.histo(i).weight

      val fitness1 = RegressionTree.calcVar(ls, lss, lc)
      val fitness2 = RegressionTree.calcVar(rs, rss, rc)

      val fitness = fitness0 + fitness1 + fitness2
      if (bestImpurity > fitness) {
        bestImpurity = fitness
        val w = h.histo(i).weight + h.histo(i+1).weight
        bestSplit = h.histo(i).center * (h.histo(i).weight/w) + h.histo(i+1).center * (h.histo(i+1).weight/w)
      }
    }
    
    val gain = {
      if (bestImpurity == Double.MaxValue) {
        0.0
      } else {
        fitnessOrigin - bestImpurity
      }
    }

    (bestSplit, bestImpurity, gain)
  }
}