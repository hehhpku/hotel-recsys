package com.metiuan.dataapp.gbdt

import org.scalatest.FunSuite
import com.meituan.dataapp.gbdt.Bin
import com.meituan.dataapp.gbdt.Histogram

class HistogramTest extends FunSuite {
  test("Histogram Merge Test") {
    val h1 = Array.ofDim[Bin](10)
    h1(0) = Bin(1.0, 2.0, 2.0, 3.0, 1.0)
    h1(1) = Bin(3.0, 2.0, 2.0, 3.0, 1.0)
    val x1 = Histogram(h1)

    val h2 = Array.ofDim[Bin](10)
    h2(0) = Bin(1.0, 2.0, 2.0, 3.0, 1.0)
    h2(1) = Bin(1.5, 4.0, 2.0, 3.0, 1.0)
    val x2 = Histogram(h2)

    val x3 = Histogram.merge(x1, x2, 2)
    println(x3)
  }

  test("Split Test") {
    val h = Array.ofDim[Bin](10)
    h(0) = Bin(1.0, 2.0, 2.0, 3.0, 1.0)
    h(1) = Bin(3.0, 2.0, 2.0, 3.0, 1.0)
    h(2) = Bin(5.0, 1.0, 2.0, 5.0, 1.0)
    val x = Histogram(h)

    val s = Histogram.getImpurity(x)
    println(s)
  }
}
