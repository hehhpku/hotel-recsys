package com.meituan.hbdata.rec.feature.SQLCommon

import com.meituan.hbdata.rec.feature.utils.RecDateUtils

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 1/8/16
 */
object UserPoiFeatureSQLText {

  // 用户在某段时间内支付行为
  // poi_paid: 未去重，方便以后统计用户买过poi的次数,
  // busi_type: 记录用户买的是gp、 zl
  // price_per_night: 用户购买的酒店的均价
  //language=SQL
  def getSQLTextUserPay(begin:Int): String = {
    val beginDate = RecDateUtils.getNumDayBeforeString(begin)
    val endDate = RecDateUtils.yesterday
    """
      |SELECT
      |    user_id,
      |    poi_id
      |FROM
      |    %s
      |WHERE
      |    datekey BETWEEN %s AND %s
    """.stripMargin format(TablePath.hotelFactPaymentTable, beginDate, endDate)
  }


  // 用户在某段时间的浏览行为
  // poi_viewed: poiid:time格式
  //language=SQL
  def getSQLTextUserView(begin: Int): String = {
    val beginDate = RecDateUtils.getNumDayBeforeString(begin)
    val endDate = RecDateUtils.yesterday
    """
      |SELECT
      |	   user_id,
      |    poi_id
      |FROM
      |    %s
      |WHERE
      |    datekey BETWEEN %s AND %s
      | AND
      |    user_id IS NOT NULL
      | AND
      |    poi_id IS NOT NULL
    """.stripMargin format(TablePath.hotelUserViewPoiTable,beginDate,endDate)
  }

  //用户在某段时间内的收藏行为
  // poi_collections: 逗号分隔的poi列表
  //language=SQL
  def getSQLTextUserCollect(begin:Int): String = {
    val beginTimeStamp = RecDateUtils.getTimeStamp(begin)
    """
      |SELECT
      |    a.user_id,
      |    a.poi_id
      |FROM
      |(
      |    SELECT
      |    	  poi_id,
      |    	  user_id
      |    FROM
      |    	  %s
      |    WHERE
      |    	  status = 1
      |       AND UNIX_TIMESTAMP(create_time) >= %s
      |       AND  user_id IS  NOT NULL
      |       AND  poi_id IS NOT NULL
      |) a
      |JOIN
      |  	%s dp
      |ON
      |  	a.poi_id = dp.poi_id
    """.stripMargin format( TablePath.hotelUserCollectTable, beginTimeStamp, TablePath.hotelDimPoiTable )
  }

}
