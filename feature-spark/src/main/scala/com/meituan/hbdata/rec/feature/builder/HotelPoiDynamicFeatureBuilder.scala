package com.meituan.hbdata.rec.feature.builder

import com.meituan.hbdata.rec.feature.SQLCommon.PoiFeatureSQLText
import com.meituan.hbdata.rec.feature.utils.{RecDateUtils, ParameterUtility}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{Logging, SparkContext, SparkConf}

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/17/15
 */
object HotelPoiDynamicFeatureBuilder extends Logging{

  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setAppName("HotelPoiDynamicFeatureBuilder")
    val sc = new SparkContext(conf)
    val hc = new HiveContext(sc)
    val argMap = ParameterUtility.parseArgs(args)

    logInfo("[INFO] HotelPoiDynamicFeatureBuilder()")

    //酒店poi的基本信息
    val hotelPoiDF = hc.sql(PoiFeatureSQLText.sqlTextHotelPoi)

    //poi的评分信息
    val poiScoreDF = hc.sql(PoiFeatureSQLText.sqlTextPoiScore)

    //poi 90天内累计销售、退款及消费数量
    val poiConsume90DF = hc.sql(PoiFeatureSQLText.getSqlTextPoiConsume(90))

    //poi 7天内累计销售、退款及消费数量
    val poiConsume7DF = hc.sql(PoiFeatureSQLText.getSqlTextPoiConsume(7))

    //POI 历史消费券数及消费金额
    val poiSummaryRevenueDF  = hc.sql(PoiFeatureSQLText.sqlTextPoiSummaryRevenue)

    //POI CTR、CVR类型特征 TODO 修改时间
    val poiCtr7dayDF = hc.sql(PoiFeatureSQLText.getsqlTextCTR_NumDate(7))
    val poiCtr90dayDF = hc.sql(PoiFeatureSQLText.getsqlTextCTR_NumDate(90))

    //POI 全业务类型酒店筛选入口下的CTR、CVR特征 TODO 修改时间
    val poiCtrAll7dayDF = hc.sql(PoiFeatureSQLText.getSqlTextCTRAll_NumDate(7))
    val poiCtrAll90dayDF = hc.sql(PoiFeatureSQLText.getSqlTextCTRAll_NumDate(90))

    //POI position-bias ctr feature
    val poiCtrPositionDF = hc.sql(PoiFeatureSQLText.sqlTextPoiCTRPositionBias)

    val outputPath = argMap("outputPath")
    logInfo("[INFO] [RUN] JOIN")
    val poiFeatureDF = hotelPoiDF.join(poiScoreDF, "poi_id")
      .join(poiConsume7DF, "poi_id")
      .join(poiConsume90DF, "poi_id")
      .join(poiSummaryRevenueDF,"poi_id")
      .join(poiCtr7dayDF, "poi_id")
      .join(poiCtr90dayDF, "poi_id")
      .join(poiCtrAll7dayDF, "poi_id")
      .join(poiCtrAll90dayDF, "poi_id")
      .join(poiCtrPositionDF, "poi_id")

    poiFeatureDF.map(_.mkString("\t"))
      .repartition(9)
      .saveAsTextFile(outputPath + "/dt=" + RecDateUtils.yesterday)

    sc.stop()
  }

}
