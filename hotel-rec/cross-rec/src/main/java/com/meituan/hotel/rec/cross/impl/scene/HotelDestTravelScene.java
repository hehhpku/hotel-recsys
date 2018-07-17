package com.meituan.hotel.rec.cross.impl.scene;

import com.meituan.hotel.rec.cross.CrossHotelOrderInfo;
import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import com.meituan.hotel.rec.cross.PoiInfo;
import com.meituan.hotel.rec.cross.impl.Util.AvailClass;
import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.JMonitorKey;
import com.meituan.hotel.rec.cross.impl.travel.TravelRecommendResult;
import com.meituan.hotel.rec.cross.impl.travel.NearbyTravelRecommend;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import com.meituan.jmonitor.JMonitor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 推荐用户入住地旅游单
 * Created by zuolin on 15/11/7.
 */
public class HotelDestTravelScene extends SceneClass {

    private static final Logger logger = LoggerFactory.getLogger(HotelDestTravelScene.class);
    private NearbyTravelRecommend nearbyTravelRecommend = new NearbyTravelRecommend();

    @Override
    public List<TravelOrHotelPoiInfo> recommendTravelOrHotel(JSONObject jsodLog, CrossRecRequest request, CrossRecResponse response) {
        long startTime = System.currentTimeMillis();
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        List<CrossHotelOrderInfo> hotelOrderInfo = request.getHotelOrderInfo();
        //获取酒店信息
        PoiInfo poiInfo = hotelOrderInfo.get(0).getPoiids().get(0);
        long hotelPoiId = poiInfo.getPoiid();
        int cityid = poiInfo.getCityId();
        double lat = poiInfo.getLatitude();
        double lng = poiInfo.getLongitude();

        List<TravelOrHotelPoiInfo> nearPoiList = new ArrayList<TravelOrHotelPoiInfo>();

        if (AvailField.TRAVEL_CF_HOTEL_VIEW_STRATEGY.equals(this.getStrategy())) {
            nearPoiList = nearbyTravelRecommend.getTravelCFFromHotel(hotelPoiId, lat, lng, response, this.getStrategy(), -1);
        } else if (AvailField.FIRST_RANK_STRATEGY.equals(this.getStrategy())) {
            nearPoiList = nearbyTravelRecommend.
                    getNearbyTravelPoiList(cityid, lat, lng, response);
        } else if (AvailField.TRAVEL_CF_HOTEL_LR_STRATEGY.equals(this.getStrategy())) {
            nearPoiList = nearbyTravelRecommend.getTravelCFFromHotel(hotelPoiId, lat, lng, response, this.getStrategy(), 1);
        } else {
            nearPoiList = nearbyTravelRecommend.getTravelCFFromHotel(hotelPoiId, lat, lng, response, this.getStrategy(), -1);
        }

        List<TravelOrHotelPoiInfo> addList = nearbyTravelRecommend.getNearbyTravelPoiList(cityid, lat, lng);
        poiList = TravelRecommendResult.recResult(jsodLog, nearPoiList, addList, cityid, request, 1);
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(AvailField.HOTEL_DEST_TRAVEL_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.HOTEL_DEST_TRAVEL_TIME, costTime);
        return poiList;
    }
}