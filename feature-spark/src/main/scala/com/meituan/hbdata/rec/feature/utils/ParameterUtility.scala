package com.meituan.hbdata.rec.feature.utils

import scala.collection.mutable

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/14/15
 */
object ParameterUtility {
  /**
   * Parse the arguments that input from command line to map
   * @param args arguments string
   * @return arguments map
   */
  def parseArgs(args: Array[String]): mutable.HashMap[String, String] = {
    val argsMap = new mutable.HashMap[String, String]() {
      override def default(key: String) = "Not set!"
    }

    for (arg <- args) {
      val keyValue = arg.split("=")
      if (keyValue.length == 2) {
        argsMap += (keyValue(0) -> keyValue(1))
      }
    }
    argsMap
  }
}
