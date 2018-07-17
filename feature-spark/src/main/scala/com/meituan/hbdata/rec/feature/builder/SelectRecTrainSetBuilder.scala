package com.meituan.hbdata.rec.feature.builder

import com.meituan.hbdata.rec.feature.SQLCommon.{TableCreator, SelectRecSQLText}
import com.meituan.hbdata.rec.feature.utils.{RecDateUtils, ParameterUtility}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkContext, SparkConf}

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/23/15
 */
object SelectRecTrainSetBuilder {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setAppName("HotelPoiDynamicFeatureBuilder")
    val sc = new SparkContext(conf)
    val hc = new HiveContext(sc)

    hc.sql("use ns_hotel")
    hc.sql("ALTER TABLE mining_rec_selrec_service ADD IF NOT EXISTS PARTITION(dt=%s)" format RecDateUtils.today )

    //    hc.sql("DROP TABLE IF EXISTS mining_rec_selrec_service" )

    val argMap = ParameterUtility.parseArgs(args)
    val outputPath = argMap("outputPath")

    //服务日志
    val logDF = hc.sql(SelectRecSQLText.sqlTextSelectRecLog)
    //点击日志
    val viewDF = hc.sql(SelectRecSQLText.sqlTextSelectRecView)
    //下单日志
    val orderDF = hc.sql(SelectRecSQLText.sqlTextSelectRecOrder)
    //支付日志
    val payDF = hc.sql(SelectRecSQLText.sqlTextSelectRecPay)
    //uuid-userid
    val uuidUserIdDF = hc.sql(SelectRecSQLText.sqlTextUuidUserid)

    //服务样本，仅要有点击行为的样本
    val actionDF =viewDF.unionAll(orderDF)
      .unionAll(payDF)
      .groupBy("uuid","poi_id")
      .agg(Map("time"->"max","isView"->"max","isOrder"->"max","isPay"->"max"))
    logDF.join(uuidUserIdDF,"uuid").join(actionDF,"uuid")
      .map(_.mkString("\t")).repartition(9)
      .saveAsTextFile(outputPath + "/dt=" + RecDateUtils.yesterday)

//    hc.sql(TableCreator.sqlTextCreateSelRecServiceTable)

    sc.stop()

  }
}
