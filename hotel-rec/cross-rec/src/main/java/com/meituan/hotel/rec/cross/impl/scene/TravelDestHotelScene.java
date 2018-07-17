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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zuolin on 15/11/20.
 */
public class TravelDestHotelScene extends SceneClass {
    public static Logger logger = LoggerFactory.getLogger(TravelDestHotelScene.class);
    private NearbyHotelRecommend nearbyHotelRecommend = new NearbyHotelRecommend();

    /**
     * 根据旅游单的目的地推荐旅游地酒店
     * @param jsodLog
     * @param request
     * @return
     */
    @Override
    public List<TravelOrHotelPoiInfo> recommendTravelOrHotel(JSONObject jsodLog, CrossRecRequest request, CrossRecResponse response) {
        long startTime = System.currentTimeMillis();

        int cityid;
        double lat;
        double lng;
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        try{
            cityid = request.getTravelOrderInfo().get(0).
                    getPoiids().get(0).getCityId();
            lat = request.getTravelOrderInfo().get(0).
                    getPoiids().get(0).getLatitude();
            lng = request.getTravelOrderInfo().get(0).
                    getPoiids().get(0).getLongitude();
        }catch (Exception e){
            logger.error(AvailField.CITY_EXPLAIN_EXCEPTION + e.toString());
            long costTime = System.currentTimeMillis() - startTime;
            logger.info(AvailField.TRAVEL_DEST_HOTEL_TIME + costTime + "ms");
            JMonitor.add(JMonitorKey.TRAVEL_DEST_HOTEL_TIME, costTime);
            return poiList;
        }
        List<TravelOrHotelPoiInfo> nearPoiList = nearbyHotelRecommend.
                getNearbyHotelPoiList(cityid, lat, lng, response);
        poiList = HotelRecommendResult.recResult(jsodLog, nearPoiList, cityid);
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(AvailField.TRAVEL_DEST_HOTEL_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.TRAVEL_DEST_HOTEL_TIME, costTime);
        return poiList;
    }
}
