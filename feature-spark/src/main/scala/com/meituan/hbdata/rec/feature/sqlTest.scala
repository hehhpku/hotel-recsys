package com.meituan.hbdata.rec.feature

import com.meituan.hbdata.rec.feature.utils.ParameterUtility
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkContext, SparkConf}

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/14/15
 */
object sqlTest {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setAppName("submitExample")
    val sc = new SparkContext(conf)
    val hc = new HiveContext(sc)
    val argMap = ParameterUtility.parseArgs(args)
//    val inputPath = argMap("inputPath")
    val outputPath = argMap("outputPath") + "_sql_" + System.currentTimeMillis()
    val table = argMap("table")
    val sqlText = "select * from %s where datekey=20151213 limit 100" format(table)
    hc.sql(sqlText).rdd.saveAsTextFile(outputPath)
    sc.stop()
  }
}
