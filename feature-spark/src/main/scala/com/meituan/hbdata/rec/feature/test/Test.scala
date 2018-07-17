package com.meituan.hbdata.rec.feature.test


/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/17/15
 */
object Test {
  def main(args: Array[String]) {
    val  a = Map(1 -> 2)
    val b = a.updated(1,5) + (3->5)
    println(a)
    println(b)
  }
}
