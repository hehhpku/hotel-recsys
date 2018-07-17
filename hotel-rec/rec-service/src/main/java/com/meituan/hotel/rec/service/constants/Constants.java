package com.meituan.hotel.rec.service.constants;

/**
 * Created by hehuihui on 3/15/16
 */
public class Constants {
    public static final String LOG_PREFIX = "hotelRec.";

    public static final String STID_PARAM_CHECKIN_CITY = "checkin_city"; // 代表入住城市，即为前置页筛选选中的城市信息
    public static final String STID_PARAM_LOCATION_CITY = "location_city"; // 代表定位城市，即为设备所在城市

    public static final String SEPERATOR_COMMA = ",";
    public static final String SEPERATOR_EQ = "=";
    public static final String SEPERATOR_SPACE = " ";

    public static final int MAX_QUERY_LENGTH = 50;

    public static final int MIN_RECALL_NUM = 200;
    public static final double MAX_DISTANCE = 30000;

    public static final String HOUR_ROOM = "HR";
    public static final String DAY_ROOM = "DR";
    //sorting method
    public static final String SMART = "smart";

    //log key
    public static final String DISTANCE_TO_REQ = "distanceToReq";
    public static final String CORR_SCORE = "corrS";
    public static final String SALE_SCORE = "saleS";
    public static final String PRICE_SCORE = "priceS";
    public static final String DIS_SCORE = "distanceS";
    public static final String MARK_SCORE = "markS";
    public static final String RAW_SCORE = "rawS";

    /** 酒店星级
     “0”：国家旅游局颁布五星级证书
     “1”：豪华（按五星级标准建造）
     “2”：国家旅游局颁布四星级证书
     “3”：高档（按四星级标准建造）
     “4”：国家旅游局颁布三星级证书
     “5”：舒适型（按三星级标准建造）
     “6”：经济型
     “7”：低档
     */
    public static final int DEFAULT_HOTEL_STAR = 4;
    public static final int LOW_HOTEL_STAR = 6;

    public static String getSortingMethod(String s){
        String sortingMethod = SMART;
        if ("smart".equalsIgnoreCase(s)){
            sortingMethod = SMART;
        }
        return sortingMethod;
    }

}
