package com.meituan.hotel.rec.cross.impl.scene;

import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.JMonitorKey;
import com.meituan.hotel.rec.cross.impl.travel.TravelRecommendResult;
import com.meituan.hotel.rec.cross.impl.travel.NearbyTravelRecommend;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import com.meituan.jmonitor.JMonitor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 根据用户的定位信息推荐
 * Created by zuolin on 15/11/16.
 */
public class TravelGpsTravelScene extends SceneClass{
    private static final Logger logger = LoggerFactory.getLogger(TravelGpsTravelScene.class);
    private NearbyTravelRecommend nearbyTravelRecommend = new NearbyTravelRecommend();
    /**
     * 由用户的定位进行推荐
     * @param request
     * @return
     */
    @Override
    public List<TravelOrHotelPoiInfo> recommendTravelOrHotel(JSONObject jsodLog, CrossRecRequest request, CrossRecResponse response){

        long startTime = System.currentTimeMillis();
        //获取用户定位信息
        int userOrderCityid = request.getUserOrderCityid();
        double userOrderLat = request.getUserLat();
        double userOrderLng = request.getUserLng();
        //定位用户位置附近的旅游poi
        List<TravelOrHotelPoiInfo> nearPoiList = nearbyTravelRecommend.
                getNearbyTravelPoiList(userOrderCityid, userOrderLat, userOrderLng, response);
        List<TravelOrHotelPoiInfo> poiList = TravelRecommendResult.recResult(jsodLog, nearPoiList, null, userOrderCityid, request, -1);
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(AvailField.TRAVEL_GPS_TRAVEL_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.TRAVEL_GPS_TRAVEL_TIME, costTime);
        return poiList;
    }
}
