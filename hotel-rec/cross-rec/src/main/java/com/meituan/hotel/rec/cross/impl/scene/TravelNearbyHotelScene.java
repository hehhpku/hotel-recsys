package com.meituan.hotel.rec.cross.impl.scene;

import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import com.meituan.hotel.rec.cross.CrossTravelOrderInfo;
import com.meituan.hotel.rec.cross.PoiInfo;
import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.JMonitorKey;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import com.meituan.hotel.rec.cross.impl.hotel.HotelRecommendResult;
import com.meituan.hotel.rec.cross.impl.hotel.NearbyHotelRecommend;
import com.meituan.jmonitor.JMonitor;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zuolin on 15/11/20.
 */
public class TravelNearbyHotelScene extends SceneClass {
    private static final Logger logger = LoggerFactory.getLogger(TravelNearbyHotelScene.class);
    private NearbyHotelRecommend nearbyHotelRecommend = new NearbyHotelRecommend();

    /**
     * 根据旅游景点位置推荐附近的酒店poi
     * @param jsodLog
     * @param request
     * @return
     */
    @Override
    public List<TravelOrHotelPoiInfo> recommendTravelOrHotel(JSONObject jsodLog, CrossRecRequest request, CrossRecResponse response) {
        long startTime = System.currentTimeMillis();
        List<CrossTravelOrderInfo> travelOrderInfo = request.getTravelOrderInfo();
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        if(CollectionUtils.isEmpty(travelOrderInfo)){
            logger.warn(AvailField.TRAVEL_ORDERINFO_WARNING + request);
            long costTime = System.currentTimeMillis() - startTime;
            logger.info(AvailField.TRAVEL_NEARBY_HOTEL_TIME + costTime + "ms");
            JMonitor.add(JMonitorKey.TRAVEL_NEARBY_HOTEL_TIME, costTime);
            return poiList;
        }

        //获取旅游信息
        PoiInfo poiInfo = travelOrderInfo.get(0).getPoiids().get(0);
        int cityid = poiInfo.getCityId();
        double lat = poiInfo.getLatitude();
        double lng = poiInfo.getLongitude();

        List<TravelOrHotelPoiInfo> nearPoiList = nearbyHotelRecommend.
                getNearbyHotelPoiList(cityid, lat, lng, response);
        poiList = HotelRecommendResult.recResult(jsodLog, nearPoiList, cityid);
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(AvailField.TRAVEL_NEARBY_HOTEL_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.TRAVEL_NEARBY_HOTEL_TIME, costTime);
        return poiList;
    }
}
