package com.meituan.hbdata.rec.feature.SQLCommon

import com.meituan.hbdata.rec.feature.utils.RecDateUtils

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/23/15
 */
object SelectRecSQLText {

   //select-rec 的日志
   //language=SQL
   val sqlTextSelectRecLog =
   """
     |SELECT
     |    uuid,
     |    MAX(request) request,
     |    response,
     |    MAX(scoredetail) scoreDetail,
     |    MAX(get_json_object(request,'$.cityId')) cityId,
     |    MAX(position) position,
     |    MAX(offset) offset,
     |    MAX(otherdata) otherData,
     |    MIN(time) service_time,
     |    MAX(strategy) strategy,
     |    MAX(algorithm) algorithm,
     |    MAX(globalid) globalId,
     |    MAX(feature) feature
     |FROM
     |    %s
     |WHERE
     |    dt=%s
     |GROUP BY
     |    uuid, response
  """.stripMargin format(TablePath.selRecLogTable, RecDateUtils.yesterday)

   //uuid-useid
   //language=SQL
   val sqlTextUuidUserid =
   """
     |SELECT
     |		uuid,
     |		uid userid
     |FROM
     |		%s
     |WHERE
     |		dt=%s
     |    AND uuid IS NOT NULL
     |    AND uuid != ''
     |    AND uid IS NOT NULL
     |    AND uid > 0
     |GROUP BY uid, uuid
   """.stripMargin format(TablePath.mobileLogTable,RecDateUtils.yesterday)

   //select-rec浏览
   //language=SQL
   val sqlTextSelectRecView =
   """
     |SELECT
     |    uuid,
     |    poi_id,
     |    MIN(time) time,
     |    1 AS isView,
     |    0 AS isOrder,
     |    0 AS isPay
     |FROM
     |    %s
     |WHERE
     |    datekey = %s
     |    AND get_json_object(scene_label, '$.channel') ='in'
     |    AND get_json_object(scene_label, '$.scene') ='筛选无结果推荐'
     |GROUP BY
     |    uuid,poi_id
   """.stripMargin format(TablePath.hotelViewLogTableFlag, RecDateUtils.yesterday)

   //seleect-rec下单
   //language=SQL
   val sqlTextSelectRecOrder =
   """
     |SELECT
     |    a.uuid uuid,
     |    b.poi_id poi_id,
     |    MIN(b.order_time) time,
     |    1 AS isView,
     |    1 AS isOrder,
     |    0 AS isPay
     |FROM
     |    %s a
     |JOIN
     |    %s b
     |ON a.order_id=b.order_id
     |    AND a.busi_type=b.busi_type
     |    AND b.datekey=%s
     |WHERE a.datekey=%s
     |    AND get_json_object(scene_label, '$.channel')='in'
     |    AND get_json_object(scene_label, '$.scene') ='筛选无结果推荐'
     |GROUP BY
     |    uuid, poi_id
   """.stripMargin format(TablePath.hotelOrderLogTable,TablePath.hotelOrderPlacementTable,
     RecDateUtils.yesterday,RecDateUtils.yesterday)

   //select-rec支付
   //language=SQL
   val sqlTextSelectRecPay =
      """
        |SELECT
        |    a.uuid uuid,
        |    b.poi_id poi_id,
        |    MIN(b.pay_time) time,
        |    1 AS isView,
        |    1 AS isOrder,
        |    1 AS isPay
        |FROM
        |    %s a
        |JOIN
        |    %s b
        |ON a.order_id=b.order_id
        |    AND a.busi_type=b.busi_type
        |    AND b.datekey=%s
        |WHERE a.datekey=%s
        |    AND get_json_object(scene_label, '$.channel')='in'
        |    AND get_json_object(scene_label, '$.scene') ='筛选无结果推荐'
        |GROUP BY
        |    uuid, poi_id
      """.stripMargin format(TablePath.hotelOrderLogTable,TablePath.hotelFactPaymentTable,
        RecDateUtils.yesterday,RecDateUtils.yesterday)

  //获取筛选少结果推荐的关键样本数据，包括标注
  //language=SQL
  def getSQLTextLogViewOrderPay(day:Int): String = {
    val beginDate = RecDateUtils.getNumDayBeforeString(day)
    """
       |SELECT
       |uuid,
       |get_json_object(request,'$.clientType') clienttype,
       |get_json_object(request,'$.accommodationType') accommodationtype,
       |get_json_object(request,'$.appVersion') appversion,
       |get_json_object(request,'$.strategy') strategy,
       |get_json_object(request,'$.sortingMethod') sortingmethod,
       |COALESCE(response,"[]") as response,
       |cityid cityid,
       |position position,
       |actiontime actiontime,
       |feature feature,
       |userid userid,
       |poiid poiid,
       |COALESCE(globalid,"0") as globalid,
       |COALESCE(offset,0) as offset,
       |isview isview,
       |isorder isorder,
       |ispay ispay
       |FROM %s WHERE dt = '%s'
  """.stripMargin format(TablePath.rec_selectLogViewPay,beginDate)
  }

  //获取poi的特征
  //language=SQL
  def getPoiFeature(day:Int): String={
    val beginDate = RecDateUtils.getNumDayBeforeString(day)
    """
      |SELECT
      |*
      |FROM %s
      |WHERE dt=%s
    """.stripMargin format(TablePath.rec_poiFeatureTable, beginDate)
        }
}
