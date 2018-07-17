package com.meituan.hbdata.rec.feature.SQLCommon

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/14/15
 */
object TablePath {

  val selRecLogTable = "log.hotel_selectRec_log"
  val hotelViewLogTableFlag = "ba_hotel.detail_mobile_poi_view_flag"
  val hotelUserViewPoiTable = "ba_hotel.detail_mobile_poi_view"
  val hotelUserCollectTable = "fact.poi_collections"
  val mobileLogTable = "log.mobilelog"
  val hotelOrderLogTable = "ba_hotel.detail_mobile_orderinfo_all_flag"
  val hotelOrderPlacementTable = "ba_hotel.fact_orderplacement_all"
  val hotelFactPaymentTable = "ba_hotel.fact_orderpayment_all"


  val hotelDimPoiTable = "ba_hotel.dim_poi"
  val poiScoreTable = "origin_cos.poiop__poi_score"
  val poiTopicTable = "ba_hotel.topic_poi_date"
  val poiSumRevenueTable = "ba_hotel.summary_poi_revenue_daily"
  val poiTrackingTable ="ba_hotel.rerank_poi_tracking"
  val poiTrackingAllTable ="ba_hotel.rerank_poi_tracking_all"
  val poiCtrPositionBiasTable = "ns_hotel.rerank_poi_ctr_position_bias"
  
  val rec_poiFeatureTable = "ns_hotel.rec_hotel_poi_feature"
  val rec_selectLogViewPay = "ns_hotel.mining_rec_selrec_service"

}
