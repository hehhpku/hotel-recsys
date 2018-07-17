package com.meituan.hotel.rec.service.constants;

/**
 * Author: hehuihui@meituan.com Date: 3/14/16
 */
public class JmonitorKey {
    public static final String PREFIX = "hotel.rec.";
    public static final String GET_POI_FROM_RECDATA_TIME = getTimeKey("GET_POI_FROM_RECDATA_TIME");
    public static final String GET_POI_FROM_MEDIS_TIME = getTimeKey("GET_POI_FROM_MEDIS_TIME");
    public static final String ROOM_STATUS_TIME = getTimeKey("ROOM_STATUS_TIME");
    public static final String POI_SINAI_TIME = getTimeKey("POI_SINAI_TIME");
    public static final String RAW_RANK_ROUTER_TIME = getTimeKey("RAW_RANK_ROUTER_TIME");
    public static final String SERVICE_TIME = getTimeKey("SERVICE_TIME");
    public static final String RAW_RANK_CORR_TIME = getTimeKey("RAW_RANK_CORR_TIME");

    //datahub
    public static final String GET_USERID_FROM_UUID_TIME = getTimeKey("GET_USERID_FROM_UUID_TIME");
    public static final String GET_USERID_FROM_UUID_EXP = getExpKey("GET_USERID_FROM_UUID_EXP");
    //strategy
    public static final String GET_STRATEGY_TIME = getTimeKey("GET_STRATEGY_TIME");
    public static final String GET_STRATEGY_EXP = getTimeKey("GET_STRATEGY_EXP");
    //recall
    public static final String RECALL_ROUTER_TIME = getTimeKey("RECALL_ROUTER_TIME");
    public static final String RECALL_ROUTER_EXP = getExpKey("RECALL_ROUTER_EXP");
    public static final String BRAND_RECALL_TIME = getTimeKey("BRAND_RECALL_TIME");
    public static final String CITY_HOT_POI_RECALL_TIME = getTimeKey("CITY_HOT_POI_RECALL_TIME");
    public static final String CITY_POI_RECALL_CORR_TIME = getTimeKey("CITY_POI_RECALL_CORR_TIME");
    public static final String CITY_POI_RECALL_CORR_MOD_TIME = getTimeKey("CITY_POI_RECALL_CORR_MOD_TIME");
    public static final String RECENT_VIEW_CORR_RECALL_TIME = getTimeKey("RECENT_VIEW_CORR_RECALL_TIME");
    public static final String CITY_POI_RECALL_TIME = getTimeKey("CITY_POI_RECALL_TIME");
    //filter
    public static final String FILTER_TIME = getTimeKey("FILTER_TIME");
    public static final String FILTER_EXP = getTimeKey("FILTER_EXP");
    //init
    public static final String INIT_TIME = getTimeKey("INIT_TIME");
    //fatal error
    public static final String FATAL_ERROR = getExpKey("FATAL_ERROR");
    //rerank
    public static final String RERANK = getTimeKey("RERANK");

    // 高星酒店
    public static final String DAEDALUS_EXP = getExpKey("DAEDALUS");
    public static final String HIGH_STAR_RULE_TIME = getTimeKey("HIGH_STAR_RULE");

    //房态
    public static final String ROOM_STATE_TIMEOUT = getExpKey("ROOM_STATE_TIMEOUT");
    //hotel.rec.ROOM_STATE_TIMEOUT.exception.count

    //timeout
    public static final String TIMEOUT_200_MS = getTimeKey("TIMEOUT_200_MS");

    public static String getTimeKey(String key){
        return PREFIX + key + ".time";
    }

    public static String getExpKey(String key){
        return PREFIX + key + ".exception";
    }


}
