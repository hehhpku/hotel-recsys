package com.meituan.hbdata.rec.feature.builder

import com.meituan.hbdata.rec.feature.SQLCommon.UserPoiFeatureSQLText
import com.meituan.hbdata.rec.feature.udaf.GroupConcat
import com.meituan.hbdata.rec.feature.utils.ParameterUtility
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.Column

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer


/**
 *
 * Author: hehuihui@meituan.com
 * Date: 1/7/16
 */
object BuildUserFeature {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setAppName("BuildUserFeature")
    val sc = new SparkContext(conf)
    val hc = new HiveContext(sc)
    val group_concat = hc.udf.register("group_concat",new GroupConcat())

    val argsMap = ParameterUtility.parseArgs(args)
    val outputPath = argsMap("outputPath")

    val userPayDF = hc.sql(UserPoiFeatureSQLText.getSQLTextUserPay(30))
      .groupBy("user_id")
      .agg(group_concat(new Column("poi_id")))

    val userViewDF = hc.sql(UserPoiFeatureSQLText.getSQLTextUserView(30))
      .groupBy("user_id")
      .agg(group_concat(new Column("poi_id")))

    val userCollectDF = hc.sql(UserPoiFeatureSQLText.getSQLTextUserCollect(30))
      .groupBy("user_id")
      .agg(group_concat(new Column("poi_id")))

    val userFeatureDF = userPayDF.join(userViewDF,"user_id").join(userCollectDF,"user_id")
    userFeatureDF.rdd
      .map(_.mkString("\t"))
      .repartition(50)
      .saveAsTextFile(outputPath)

    sc.stop()
  }
}
