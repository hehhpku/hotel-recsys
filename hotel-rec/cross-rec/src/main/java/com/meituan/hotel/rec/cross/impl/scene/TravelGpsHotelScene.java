package com.meituan.hotel.rec.cross.impl.scene;

import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.JMonitorKey;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import com.meituan.hotel.rec.cross.impl.hotel.HotelRecommendResult;
import com.meituan.hotel.rec.cross.impl.hotel.NearbyHotelRecommend;
import com.meituan.jmonitor.JMonitor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by zuolin on 15/11/20.
 */
public class TravelGpsHotelScene extends SceneClass {
    private static final Logger logger = LoggerFactory.getLogger(TravelGpsHotelScene.class);
    private NearbyHotelRecommend nearbyHotelRecommend = new NearbyHotelRecommend();

    /**
     * 根据用户的定位地推荐周围酒店
     * @param jsodLog
     * @param request
     * @return
     */
    @Override
    public List<TravelOrHotelPoiInfo> recommendTravelOrHotel(JSONObject jsodLog, CrossRecRequest request, CrossRecResponse response) {
        long startTime = System.currentTimeMillis();

        //获取用户定位信息
        int userOrderCityid = request.getUserOrderCityid();
        double userOrderLat = request.getUserLat();
        double userOrderLng = request.getUserLng();

        //定位用户位置附近的酒店poi
        List<TravelOrHotelPoiInfo> nearPoiList = nearbyHotelRecommend.
                getNearbyHotelPoiList(userOrderCityid, userOrderLat, userOrderLng, response);
        List<TravelOrHotelPoiInfo> poiList = HotelRecommendResult.recResult(jsodLog, nearPoiList, userOrderCityid);
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(AvailField.TRAVEL_GPS_TRAVEL_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.TRAVEL_GPS_TRAVEL_TIME, costTime);
        return poiList;
    }
}
