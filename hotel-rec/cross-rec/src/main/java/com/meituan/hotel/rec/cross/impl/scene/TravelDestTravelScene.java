package com.meituan.hotel.rec.cross.impl.scene;

import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import com.meituan.hotel.rec.cross.CrossTravelOrderInfo;
import com.meituan.hotel.rec.cross.PoiInfo;
import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.JMonitorKey;
import com.meituan.hotel.rec.cross.impl.travel.TravelRecommendResult;
import com.meituan.hotel.rec.cross.impl.travel.NearbyTravelRecommend;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import com.meituan.jmonitor.JMonitor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据旅游订单的目的地推荐
 * Created by zuolin on 15/11/16.
 */
public class TravelDestTravelScene extends SceneClass{
    private static final Logger logger = LoggerFactory.getLogger(TravelDestTravelScene.class);
    private NearbyTravelRecommend nearbyTravelRecommend = new NearbyTravelRecommend();

    /**
     * 根据旅游订单的目的地城市进行旅游推荐
     * @param request
     * @return
     */
    @Override
    public List<TravelOrHotelPoiInfo> recommendTravelOrHotel(JSONObject jsodLog, CrossRecRequest request, CrossRecResponse response){
        long startTime = System.currentTimeMillis();
        List<CrossTravelOrderInfo> travelOrderInfo = request.getTravelOrderInfo();
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();

        //获取旅游信息
        PoiInfo poiInfo = travelOrderInfo.get(0).getPoiids().get(0);
        int cityid = poiInfo.getCityId();
        double lat = poiInfo.getLatitude();
        double lng = poiInfo.getLongitude();

        List<TravelOrHotelPoiInfo> nearPoiList = nearbyTravelRecommend.
                getNearbyTravelPoiList(cityid, lat, lng, response);
        List<TravelOrHotelPoiInfo> addList = nearbyTravelRecommend.getNearbyTravelPoiList(cityid, lat, lng);
        poiList = TravelRecommendResult.recResult(jsodLog, nearPoiList, addList, cityid, request, 0);
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(AvailField.TRAVEL_DEST_TRAVEL_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.TRAVEL_DEST_TRAVEL_TIME, costTime);
        return poiList;
    }
}
