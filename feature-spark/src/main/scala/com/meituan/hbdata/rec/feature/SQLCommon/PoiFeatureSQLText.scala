package com.meituan.hbdata.rec.feature.SQLCommon

import com.meituan.hbdata.rec.feature.utils.RecDateUtils

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/19/15
 */
object PoiFeatureSQLText {
  //日期常量
  val yesterday = RecDateUtils.getNumDayBeforeString(1)
  private val day7Before = RecDateUtils.getNumDayBeforeString(7)
  private val day2Before = RecDateUtils.getNumDayBeforeString(2)
  private val day30Before = RecDateUtils.getNumDayBeforeString(30)
  private val day90Before = RecDateUtils.getNumDayBeforeString(90)

  //poi的基本信息
  //language=SQL
  val sqlTextHotelPoi =
  """
    |SELECT
    |    main_poi_id poi_id,
    |    MAX(name) name,
    |    MAX(city_id) city_id,
    |    MAX(brand_id) brand_id,
    |    MAX(latitude) latitude,
    |    MAX(longitude) longitude
    |FROM
    |	   %s
    |WHERE
    |    poi_type = 0
    |GROUP BY main_poi_id
  """.stripMargin format TablePath.hotelDimPoiTable

  //poi历史评分，评价人数
  //language=SQL
  val sqlTextPoiScore =
  """
    |SELECT
    |	   poiid poi_id,
    |    sum(avgscore)/COUNT(avgScore) avgScore,
    |    sum(fbcount) scoreCnt,
    |    sum(all_comment_num) scoreCntTotal,
    |    sum(all_has_content_comment_num) commentCntTotal,
    |    sum(all_avg_score)/COUNT(all_avg_score) avgScoreTotal
    |FROM
    |	origin_cos.poiop__poi_score
    |GROUP BY poiid
  """.stripMargin format TablePath.poiScoreTable

  //poi dayNum天内累计销售、退款及消费数量
  //language=SQL
  def getSqlTextPoiConsume(dayNum: Int): String = {
    val s = dayNum.toString
    val beginDate = RecDateUtils.getNumDayBeforeString(dayNum)
    val endDate = yesterday
    val table = TablePath.poiTopicTable
    s"""
       |SELECT
       |    main_poi_id poi_id,
       |    SUM(pay_order_num) %s,
       |    SUM(pay_quantity) %s,
       |    SUM(refund_order_num) %s,
       |    SUM(refund_quantity) %s,
       |    SUM(consume_quantity) %s
       |FROM $table
       |WHERE dt BETWEEN $beginDate AND $endDate
       |GROUP BY main_poi_id
     """.stripMargin format("payedOrderDay" + s + "Cnt",
      "payedCouponDay" + s + "Cnt",
      "refundOrderDay" + s + "Cnt",
      "refundCouponDay" + s + "Cnt",
      "consumeCouponDay" + s + "Cnt")
  }

  //poi历史消费券数及消费金额
  //language=SQL
  val sqlTextPoiSummaryRevenue =
    """
      |SELECT
      |    poiid poi_id,
      |    SUM(consume_revenue) consumeRevenue,
      |    SUM(consume_volume) consumeVolume
      |FROM %s
      |GROUP BY poiid
    """.stripMargin format TablePath.poiSumRevenueTable

  //poi团购业务近n天转化率
  //language=SQL
  def getsqlTextCTR_NumDate(dayNum: Int): String = {
    val s = dayNum.toString
    val beginDay = RecDateUtils.getNumDayBeforeString(dayNum)
    val endDay = yesterday
    val tablePath = TablePath.poiTrackingTable
    s"""
      |SELECT
      |    t.poiid poi_id,
      |    SUM(t.pos_ctr_day * t.pos_percent_day) %s,
      |    SUM(t.pos_cvr_day * t.pos_percent_day) %s,
      |    SUM(t.pos_ctr_day * t.pos_percent_day) * SUM(t.pos_cvr_day * t.pos_percent_day) %s,
      |    SUM(t.pos_cpr_day * t.pos_percent_day) %s
      |FROM
      |(
      |    SELECT
      |        spe.poiid,
      |        spe.position,
      |        IF(spe.impress_day_cnt = 0, 0.0, (spe.click_day_cnt / spe.impress_day_cnt)) pos_ctr_day,
      |        IF(spe.click_day_cnt = 0, 0.0, (spe.order_day_cnt / spe.click_day_cnt)) pos_cvr_day,
      |        IF(spe.click_day_cnt = 0, 0.0, (spe.pay_day_cnt / spe.click_day_cnt)) pos_cpr_day,
      |        IF(tot.impress_day_cnt_total = 0, 0.0, spe.impress_day_cnt / tot.impress_day_cnt_total) pos_percent_day
      |    FROM
      |    (
      |        SELECT
      |            poiid,
      |            position,
      |            SUM(impress_count) impress_day_cnt,
      |            SUM(click_count) click_day_cnt,
      |            SUM(order_count) order_day_cnt,
      |            SUM(pay_count) pay_day_cnt
      |        FROM
      |            $tablePath
      |        WHERE
      |            dt BETWEEN $beginDay AND $endDay
      |        GROUP BY
      |            poiid,
      |            position
      |    ) spe
      |    LEFT OUTER JOIN
      |    (
      |        SELECT
      |            poiid,
      |            SUM(impress_count) impress_day_cnt_total
      |        FROM
      |            $tablePath
      |        WHERE
      |            dt BETWEEN $beginDay AND $endDay
      |        GROUP BY
      |            poiid
      |    ) tot
      |    ON
      |        spe.poiid = tot.poiid
      |) t
      |GROUP BY
      |    t.poiid
  """.stripMargin format(
      "poiCtrDay" + s, "poiCvrDay" + s, "poiCxrDay" + s, "poiCprDay" + s)
  }

  //poi全业务近n天转化率
  //language=SQL
  def getSqlTextCTRAll_NumDate(numDate: Int): String = {
    val beginDate = RecDateUtils.getNumDayBeforeString(numDate)
    val endDate = yesterday
    val tablePath = TablePath.poiTrackingAllTable
    val s = numDate.toString
    s"""
       |SELECT
       |    t.poi_id poi_id,
       |    SUM(t.pos_ctr_day * t.pos_percent_day) %s,
       |    SUM(t.pos_cvr_day * t.pos_percent_day) %s,
       |    SUM(t.pos_ctr_day * t.pos_percent_day) * SUM(t.pos_cvr_day * t.pos_percent_day) %s,
       |    SUM(t.pos_cpr_day * t.pos_percent_day) %s
       |FROM
       |(
       |    SELECT
       |        spe.poi_id,
       |        spe.position,
       |        IF(spe.impress_day_cnt = 0, 0.0, (spe.click_day_cnt / spe.impress_day_cnt)) pos_ctr_day,
       |        IF(spe.click_day_cnt = 0, 0.0, (spe.order_day_cnt / spe.click_day_cnt)) pos_cvr_day,
       |        IF(spe.click_day_cnt = 0, 0.0, (spe.pay_day_cnt / spe.click_day_cnt)) pos_cpr_day,
       |        IF(tot.impress_day_cnt_total = 0, 0.0, spe.impress_day_cnt / tot.impress_day_cnt_total) pos_percent_day
       |    FROM
       |    (
       |        SELECT
       |            poi_id,
       |            position,
       |            SUM(impress_count) impress_day_cnt,
       |            SUM(click_count) click_day_cnt,
       |            SUM(order_count) order_day_cnt,
       |            SUM(pay_count) pay_day_cnt
       |        FROM
       |            $tablePath
       |        WHERE
       |            dt BETWEEN $beginDate AND $endDate
       |        GROUP BY
       |            poi_id,
       |            position
       |    ) spe
       |    LEFT OUTER JOIN
       |    (
       |        SELECT
       |            poi_id,
       |            SUM(impress_count) impress_day_cnt_total
       |        FROM
       |            $tablePath
       |        WHERE
       |            dt BETWEEN $beginDate AND $endDate
       |        GROUP BY
       |            poi_id
       |    ) tot
       |    ON
       |        spe.poi_id = tot.poi_id
       |) t
       |GROUP BY
       |    t.poi_id
     """.stripMargin format(
      "poiCtrAllDay" + s, "poiCvrAllDay" + s, "poiCxrAllDay" + s, "poiCprAllDay" + s)
  }

  //POI position-bias ctr feature
  //language=SQL
  val sqlTextPoiCTRPositionBias =
  s"""
    |SELECT
    |    poi_id,
    |    icr_day7 poiIcrDay7,
    |    cor_day7 poiCorDay7,
    |    opr_day7 poiOprDay7,
    |    ipr_day7 poiIprDay7,
    |    icr_day90 poiIcrDay90,
    |    cor_day90 poiCorDay90,
    |    opr_day90 poiOprDay90,
    |    ipr_day90 poiIprDay90
    |FROM
    |    %s
    |WHERE
    |    dt=$yesterday
  """.stripMargin format TablePath.poiCtrPositionBiasTable
}
