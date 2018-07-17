package com.meituan.hotel.rec.cross.impl.Util;

import com.meituan.hotel.rec.cross.impl.hotel.BasedOnHistoryRecommend;
import com.meituan.hotel.rec.cross.impl.hotel.HotHotelRecommend;
import com.meituan.hotel.rec.cross.impl.hotel.HotelPoiSort;
import com.meituan.hotel.rec.cross.impl.travel.NearbyTravelRecommend;
import com.meituan.hotel.rec.cross.impl.travel.TravelPoiFilter;
import com.meituan.hotel.rec.cross.impl.travel.HotTravelRecommend;
import com.meituan.hotel.rec.cross.impl.travel.TravelPoiSort;

import java.util.List;

/**
 * 计算距离
 * Created by zuolin on 15/11/3.
 */
public class AvailClass {
    public static final String TRAVEL_CITY_HOT_POI_KEY = "travel_city_hot_poi_";
    public static final String TRAVEL_CITY_POI_PREKEY = "travel_city_poi_";
    public static final String TARVEL_CORR_HOTEL_PREKEY = "travel_corr_hotel_";
    public static final String TARVEL_CORR_HOTEL_LOCAL_PREKEY = "travel_corr_hotel_local_";
    public static final String TARVEL_CORR_HOTEL_REMOTE_PREKEY = "travel_corr_hotel_remote_";
    public static final String TARVEL_CORR_HOTEL_PAY_PREKEY = "travel_corr_hotel_pay_";
    public static final String HOTEL_CITY_POI_PREKEY = "hbrec_city_poi_";
    public static final String HOTEL_CITY_HOT_POI_KEY = "hbrec_city_hot_poi_";
    public static final String HOTEL_DEAL_INFO_KEY = "hbrec_poi_deal_";
    public static final String USERID_HOTEL_POI_KEY = "hbrec_hotel_history_";


    public static final int MAX_REC_NUM = 100;
    public static final int MAX_CF_NUM = 50;
    public static final int MAX_TRAVEL_CITY_RADIUS = 50000;
    public static final int MAX_HOTEL_CITY_RADIUS = 10000;
    public static final int EARTH_RADIUS = 6373000;     //地球半径(单位:米)
    public static final int FACT_RETURN_NUM = 5;

    public static TravelPoiSort travelPoiSort = new TravelPoiSort();
    public static HotelPoiSort hotelPoiSort = new HotelPoiSort();
    public static TravelPoiFilter filter = new TravelPoiFilter();
    public static HotTravelRecommend hotTravelRecommend = new HotTravelRecommend();
    public static HotHotelRecommend hotHotelRecommend = new HotHotelRecommend();
    public static NearbyTravelRecommend nearbyTravelRecommend = new NearbyTravelRecommend();
    public static BasedOnHistoryRecommend historyRecommend = new BasedOnHistoryRecommend();

    /**
     * 计算两个经纬度坐标的弧面距离
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @return
     * 1. 将数据转化为弧度
     * 2. 计算球面距离
     */
    public static double calDistanceAccurate(double longitude1, double latitude1, double longitude2, double latitude2)
    {
        longitude1 = degreeToAcr(longitude1);
        longitude2 = degreeToAcr(longitude2);
        latitude1  = degreeToAcr(latitude1);
        latitude2  = degreeToAcr(latitude2);

        double distance   = 0.0;
        double dLongitude = longitude1 - longitude2;
        double dLatitude  = latitude1 - latitude2;

        double a = Math.pow((Math.sin(dLatitude / 2)), 2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.pow(Math.sin(dLongitude/2),2);
        double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        distance = c * EARTH_RADIUS;
        return distance;
    }

    /**
     * 粗略计算距离公式
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static double distanceSimplify(double lng1, double lat1, double lng2, double lat2) {
        double dx = lng1 - lng2;         // 经度差值
        double dy = lat1 - lat2;         // 纬度差值
        double b  = (lat1 + lat2) / 2.0; // 平均纬度
        double Lx = degreeToAcr(dx) * EARTH_RADIUS* Math.cos(degreeToAcr(b)); // 东西距离
        double Ly = EARTH_RADIUS * degreeToAcr(dy);                           // 南北距离
        return Math.sqrt(Lx * Lx + Ly * Ly);  // 用平面的矩形对角距离公式计算总距离
    }

    public static double degreeToAcr(double degree) {
        return degree / 180.0 * Math.PI;
    }


    /**
     * 判断候选poi是否存在于推荐列表中
     * @param candiante
     * @param poiInfo
     * @return
     */
    public static boolean isExist(List<TravelOrHotelPoiInfo> candiante, TravelOrHotelPoiInfo poiInfo){
        long poiId = poiInfo.getPoiId();
        for(TravelOrHotelPoiInfo poi : candiante){
            long tempPoiId = poi.getPoiId();
            if (poiId == tempPoiId){
                return false;
            }
        }
        return true;
    }
}
