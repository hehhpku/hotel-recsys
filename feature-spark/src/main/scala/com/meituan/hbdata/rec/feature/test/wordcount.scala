package com.meituan.hbdata.rec.feature.test

import com.meituan.hbdata.rec.feature.utils.ParameterUtility
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/9/15
 */
object wordcount {
  val conf = new SparkConf()
    .setAppName("submitExample")
  val sc = new SparkContext(conf)

  def main (args: Array[String]){
    val argMap = ParameterUtility.parseArgs(args)
    val inputPath = argMap("inputPath")
    val outputPath = argMap("outputPath") + System.currentTimeMillis()
    val distFile = sc.textFile(inputPath)
    val counts = distFile.flatMap(line => line.split("\t"))
                  .map(word => (word,1)).reduceByKey(_+_)
    counts.saveAsTextFile(outputPath)
  }
}
