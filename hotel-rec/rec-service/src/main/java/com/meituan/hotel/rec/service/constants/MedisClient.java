package com.meituan.hotel.rec.service.constants;

import com.meituan.cache.redisCluster.client.MedisFactory;
import com.meituan.cache.redisCluster.client.typeInterface.IMedis;

/**
 * Author: hehuihui@meituan.com Date: 3/14/16
 */
public class MedisClient {
    public static final IMedis medisClient = MedisFactory.getInstance("applicationContext.xml", "medisClient");

    //酒店新数据（基于sinai）
    public static final String CITY_HOT_POI_PREFIX_NEW = "hbrec_city_hot_poi_n_";
    public static final String POI_INFO_PREFIX_NEW = "hbrec_poi_info_n_";
    public static final String CITY_POI_PREFIX_NEX = "hbrec_city_poi_n_";
    public static final String PROMOTION_POI_KEY = "hbrec_promotion_poi";

    //酒店poi的动态特征
    public final static String MEDIS_HOTEL_POI_DYF_PREFIX = "hbrec_poi_dyf_";
    //酒店user的动态特征
    public final static String MEDIS_USER_DYF_PREFIX = "hbrec_user_dyf_";
    //用户实时浏览酒店记录
    public final static String MEDIS_USER_REALTIME_PREFIX = "hruv_";
    //用户poi浏览到支付的概率值
    public final static String MEDIS_POI_V2P_PREFIX = "hr_poi_v2p_";

    //关联规则数据
    public static final String POI_LLR_CORR_PREFIX_KEY= "hbrec_poi_llr_corr_";
    public static final String POI_V2P_CORR_PREFIX_KEY= "hbrec_poi_v2p_corr_";
    public static final String POI_V2P_LLR_CORR_PREFIX_KEY= "hbrec_poi_v2p_llr_corr_";

    //medis poiInfo 的index 的名称
    public static final int POI_LOWEST_PRICE = 0;
    public static final int POI_BRADNDID = 1;
    public static final int POI_AVG_SCORE = 2;
    public static final int POI_MARK_NUM = 3;
    public static final int POI_DID = 4;
    public static final int POI_LAT = 5;
    public static final int POI_LNG = 6;
    public static final int POI_CITYIDS = 7;
    public static final int POI_SALE_NUM = 8;

    //city hot medis key
    public static final  int CITY_HOT_POI_ID = 0;
    public static final  int CITY_HOT_POI_LAT = 1;
    public static final  int CITY_HOT_POI_LNG = 2;


}
