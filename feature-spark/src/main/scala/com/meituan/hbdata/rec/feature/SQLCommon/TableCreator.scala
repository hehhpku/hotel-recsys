package com.meituan.hbdata.rec.feature.SQLCommon


/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/19/15
 */
object TableCreator {
  val poiFeaturePath = "/user/hive/warehouse/ns_hotel.db/rec_hotel_poi_feature"
  //language=SQL
  val sqlTextPoiFeatureCreator =
  s"""
     |CREATE EXTERNAL TABLE IF NOT EXISTS ns_hotel.rec_hotel_poi_feature
     |(
     |        poi_id	int	COMMENT	"poi_id",
     |        name	string	COMMENT	"name",
     |        city_id	int	COMMENT	"city_id",
     |        brand_id	int	COMMENT	"brand_id",
     |        latitude	int	COMMENT	"latitude",
     |        longitude	int	COMMENT	"longitude",
     |        avgScore	double	COMMENT	"avgScore",
     |        scoreCnt	int	COMMENT	"scoreCnt",
     |        scoreCntTotal	int	COMMENT	"scoreCntTotal",
     |        commentCntTotal	int	COMMENT	"commentCntTotal",
     |        avgScoreTotal	double	COMMENT	"avgScoreTotal",
     |        payedOrderDay7Cnt	double	COMMENT	"payedOrderDay7Cnt",
     |        payedCouponDay7Cnt	double	COMMENT	"payedCouponDay7Cnt",
     |        refundOrderDay7Cnt	double	COMMENT	"refundOrderDay7Cnt",
     |        refundCouponDay7Cnt	double	COMMENT	"refundCouponDay7Cnt",
     |        consumeCouponDay7Cnt	int	COMMENT	"consumeCouponDay7Cnt",
     |        payedOrderDay90Cnt	double	COMMENT	"payedOrderDay90Cnt",
     |        payedCouponDay90Cnt	double	COMMENT	"payedCouponDay90Cnt",
     |        refundOrderDay90Cnt	double	COMMENT	"refundOrderDay90Cnt",
     |        refundCouponDay90Cnt	double	COMMENT	"refundCouponDay90Cnt",
     |        consumeCouponDay90Cnt	int	COMMENT	"consumeCouponDay90Cnt",
     |        consumeRevenue	double	COMMENT	"consumeRevenue",
     |        consumeVolume	int	COMMENT	"consumeVolume",
     |        poiCtrDay7	double	COMMENT	"poiCtrDay7",
     |        poiCvrDay7	double	COMMENT	"poiCvrDay7",
     |        poiCxrDay7	double	COMMENT	"poiCxrDay7",
     |        poiCprDay7	double	COMMENT	"poiCprDay7",
     |        poiCtrDay90	double	COMMENT	"poiCtrDay90",
     |        poiCvrDay90	double	COMMENT	"poiCvrDay90",
     |        poiCxrDay90	double	COMMENT	"poiCxrDay90",
     |        poiCprDay90	double	COMMENT	"poiCprDay90",
     |        poiCtrAllDay7	double	COMMENT	"poiCtrAllDay7",
     |        poiCvrAllDay7	double	COMMENT	"poiCvrAllDay7",
     |        poiCxrAllDay7	double	COMMENT	"poiCxrAllDay7",
     |        poiCprAllDay7	double	COMMENT	"poiCprAllDay7",
     |        poiCtrAllDay90	double	COMMENT	"poiCtrAllDay90",
     |        poiCvrAllDay90	double	COMMENT	"poiCvrAllDay90",
     |        poiCxrAllDay90	double	COMMENT	"poiCxrAllDay90",
     |        poiCprAllDay90	double	COMMENT	"poiCprAllDay90",
     |        poiIcrDay7	double	COMMENT	"poiIcrDay7",
     |        poiCorDay7	double	COMMENT	"poiCorDay7",
     |        poiOprDay7	double	COMMENT	"poiOprDay7",
     |        poiIprDay7	double	COMMENT	"poiIprDay7",
     |        poiIcrDay90	double	COMMENT	"poiIcrDay90",
     |        poiCorDay90	double	COMMENT	"poiCorDay90",
     |        poiOprDay90	double	COMMENT	"poiOprDay90",
     |        poiIprDay9	double	COMMENT	"poiIprDay9"
     |)COMMENT '酒店推荐服务poi特征表'
     |PARTITIONED BY (dt int )
     |ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n'
   """.stripMargin

   //language=SQL
   val sqlTextCreateSelRecServiceTable =
   """
     |    CREATE EXTERNAL TABLE IF NOT EXISTS ns_hotel.mining_rec_selrec_service
     |    (
     |        uuid	string	COMMENT	"uuid",
     |        request	string	COMMENT	"request",
     |        response	string	COMMENT	"response",
     |        scoreDetail	string	COMMENT	"scoreDetail",
     |        cityId	int	COMMENT	"cityId",
     |        position	string	COMMENT	"position",
     |        offset	int	COMMENT	"offset",
     |        otherData	string	COMMENT	"otherData",
     |        serviceTime	string	COMMENT	"serviceTime",
     |        strategy	string	COMMENT	"strategy",
     |        algorithm	string	COMMENT	"algorithm",
     |        globalId	string	COMMENT	"globalId",
     |        feature	string	COMMENT	"feature",
     |        userId int COMMENT "userId",
     |        poiId int COMMENT "poi_id",
     |        actionTime string COMMENT "actionTime",
     |        isView int COMMENT "isView",
     |        isOrder int COMMENT "isOrder",
     |        isPay int COMMENT "isPay"
     |    )COMMENT '酒店筛选少结果推荐样本'
     |    PARTITIONED BY (dt int )
     |    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n'
   """.stripMargin
}
