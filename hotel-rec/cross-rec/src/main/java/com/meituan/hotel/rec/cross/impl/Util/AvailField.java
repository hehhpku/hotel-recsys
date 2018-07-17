package com.meituan.hotel.rec.cross.impl.Util;

/**
 * Created by zuolin on 15/11/19.
 */
public class AvailField {
    //耗时字符串
    public static final String CROSS_REC_TIME = "cross.recommend.total.time ";
    public static final String HOTEL_DEST_TRAVEL_TIME = "hotel.dest.travel.time";
    public static final String HOTEL_GPS_TRAVEL_TIME = "hotel.gps.travel.time";
    public static final String HOTEL_NEARBY_TRAVEL_TIME = "hotel.nearby.travel.time";
    public static final String TRAFFIC_DEPART_TRAVEL_TIME = "travel.depart.travel.time";
    public static final String TRAFFIC_DEST_TRAVEL_TIME = "travel.dest.travel.time";
    public static final String TRAVEL_DEST_TRAVEL_TIME = "travel.dest.travel.time";
    public static final String TRAVEL_GPS_TRAVEL_TIME = "travel.gps.travel.time";
    public static final String TRAVEL_NEARBY_TRAVEL_TIME = "travel.nearby.travel.time";
    public static final String TRAFFIC_DEPART_HOTEL_TIME = "traffic.depart.hotel.time";
    public static final String TRAFFIC_DEST_HOTEL_TIME = "traffic.dest.hotel.time";
    public static final String TRAVEL_DEST_HOTEL_TIME = "ravel.dest.hotel.time";
    public static final String TRAVEL_NEARBY_HOTEL_TIME = "travel.nearby.hotel.time";

    //警告字符串
    public static final String HOTEL_ORDERINFO_WARNING = "hotel.order.warning";
    public static final String TRAVEL_ORDERINFO_WARNING = "travel.order.warning";
    public static final String CITYID_VALUE_WARNING = "cityid.value.warning";
    public static final String CITY_NOPOI_WARNING = "city.nopoi.warning";
    public static final String HOTEL_NOCORR_TRAVEL_WARNING = "hotel.nocorr.travel.warning";
    public static final String REQUEST_PARA_WARNING = "request.para.warning";
    public static final String POILIST_EMPTY_WARNING = "poilist.empty.warning";
    public static final String SCENE_DELIVER_ERROR_WARNING = "scene.deliver.error.warning";

    //异常字符串
    public static final String SCENE_EXPLAIN_EXCEPTION = "scene.explain.exception";
    public static final String CITY_EXPLAIN_EXCEPTION = "city.explain.exception";
    public static final String DATE_PARSE_EXCEPTION = "date.parse.exception";
    public static final String CONSUME_GET_EXCEPTION = "consume.get.exception";
    public static final String HOTPOI_MEDIS_EXCEPTION = "hotpoi.medis.exception";
    public static final String HOTPOI_PARSE_EXCEPTION = "hotpoi.parse.exception";
    public static final String NRARBY_POI_EXCEPTION = "neary.poi.exception";
    public static final String PRICE_SCORE_EXCEPTION = "price.calc.exception";
    public static final String EVALUATE_SCORE_EXCEPTION = "evaluate.calc.exception";
    public static final String DISTANCE_SCORE_EXCEPTION = "distance.score.exception";
    public static final String CONSUME_SCORE_EXCEPTION = "consume.score.exception";
    public static final String JSON_SAVE_EXCEPTION = "json.save.exception";
    public static final String JSON_PRINT_EXCEPTION = "json.print.exception";
    public static final String REQLOG_COLLECTION_EXCEPTION = "reqlog.collection.exception";
    public static final String RESLOG_COLLECTION_EXCEPTION = "reslog.collection.exception";
    public static final String MEDIS_READ_EXCEPTION = "medis.read.exception";
    public static final String STRATEGY_READ_EXCEPTION = "strategy.read.exception";

    //策略字符串
    public static final String FIRST_RANK_STRATEGY = "poiRankStrategy";
    public static final String TRAVEL_CF_HOTEL_VIEW_STRATEGY = "crossCorrViewStrategy" ;
    public static final String TRAVEL_CF_HOTEL_PAY_STRATEGY = "crossCorrPayStrategy" ;
    public static final String TRAVEL_CF_HOTEL_LR_STRATEGY = "crossCorrLRStrategy" ;

}
