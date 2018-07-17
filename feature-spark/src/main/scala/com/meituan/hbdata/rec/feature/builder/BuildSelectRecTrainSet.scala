package com.meituan.hbdata.rec.feature.builder

import com.meituan.hbdata.rec.feature.SQLCommon.SelectRecSQLText
import com.meituan.hbdata.rec.feature.label.RecLabels
import com.meituan.hbdata.rec.feature.modelTraining.RawSample
import com.meituan.hbdata.rec.feature.utils.{RecDateUtils, ParameterUtility}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.immutable.HashMap

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 1/7/16
 */
object BuildSelectRecTrainSet {
  val noActionLabel = new RecLabels(0, 0, 0)

  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setAppName("TrainHotelSelectRecGBDTModel")
      .set("spark.hadoop.validateOutputSpecs", "false")
    val sc = new SparkContext(conf)

    val hc = new HiveContext(sc)
    val argMap = ParameterUtility.parseArgs(args)

    val outputPath = argMap("outputPath")
    val dayIndex = argMap("dayIndex").toInt //生成哪天的样本

    // 提取筛选少结果推荐服务的日志信息，
    // 含userid, poiid, isView, isPay, isOrder, distanceToUser等在线特征
    val rawSampleDF = hc.sql(SelectRecSQLText.getSQLTextLogViewOrderPay(dayIndex))
    val rawSampleRDD = rawSampleDF.flatMap(x => {
      //推荐的poi列表
      val s_recPoiList = x.getAs[String]("response")
      var sampleMap = new HashMap[(Int, Int), RawSample]()
      if (!s_recPoiList.equalsIgnoreCase("null") && !s_recPoiList.equals("[]")) {
        val recPoiList = s_recPoiList.substring(1, s_recPoiList.length - 1).split(",").map(x => x.stripPrefix(" ").toInt).toList
        //有点击 or 下单 or 支付行为的poi
        val actionPoiId = x.getInt(x.fieldIndex("poiid"))
        val poiIdFeatureValueMap = x.getString(x.fieldIndex("feature")).split(",").map(x => {
          val info = x.stripMargin.split(":")
          val poiId = info(0).toInt
          val distanceToUser = info(1).substring(1, info(1).length - 1).toDouble
          poiId -> distanceToUser
        }).toMap
        val actionLabel = new RecLabels(x.getInt(x.fieldIndex("isview")), x.getInt(x.fieldIndex("isorder")), x.getInt(x.fieldIndex("ispay")))
        val userId = x.getAs[Int]("userid")
        for (poi_id <- recPoiList) {
          if (poi_id != actionPoiId)
            sampleMap += ((poi_id, userId) ->
              new RawSample(x, Map("distanceToUser" -> poiIdFeatureValueMap(poi_id).toString), noActionLabel, false, userId, poi_id))
          else
            sampleMap += ((poi_id, userId) ->
              new RawSample(x, Map("distanceToUser" -> poiIdFeatureValueMap(poi_id).toString), actionLabel, true, userId, poi_id))
        }
      }
      sampleMap.toList
    }).reduceByKey((x, y) => {
      if (x.isAction) x else y
    })

    val poiFeatureDF = hc.sql(SelectRecSQLText.getPoiFeature(dayIndex))
    val poiFeatureFields = poiFeatureDF.columns.toBuffer
    val poiFeatureRDD = poiFeatureDF.map( x=> {
      val poiId = x.getAs[Int]("poi_id")
      val featureValueMap = {
        x.getValuesMap[String](poiFeatureFields)
      }
      poiId -> featureValueMap
    })

    rawSampleRDD.map(x => {x._1._1 -> x._2})
      .join(poiFeatureRDD)
      .map( x => {
        val userId = x._2._1.userId
        val featureMap = x._2._1.featureValueMap ++ x._2._2
        userId -> (x._2._1 ,featureMap)
      })
      .repartition(10)
      .saveAsTextFile(outputPath + "/dt=" + RecDateUtils.yesterday)
    sc.stop()
  }
}
