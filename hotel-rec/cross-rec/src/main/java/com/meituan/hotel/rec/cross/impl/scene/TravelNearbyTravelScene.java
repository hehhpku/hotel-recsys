package com.meituan.hotel.rec.cross.impl.scene;

import com.meituan.hotel.rec.cross.*;
import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.JMonitorKey;
import com.meituan.hotel.rec.cross.impl.travel.TravelRecommendResult;
import com.meituan.hotel.rec.cross.impl.travel.NearbyTravelRecommend;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import com.meituan.jmonitor.JMonitor;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 推荐旅游景点附近的景点
 * Created by zuolin on 15/11/16.
 */
public class TravelNearbyTravelScene extends SceneClass{
    private static final Logger logger = LoggerFactory.getLogger(TravelNearbyTravelScene.class);
    private NearbyTravelRecommend nearbyTravelRecommend = new NearbyTravelRecommend();
    /**
     * 推荐旅游景点周围的旅游景点
     * @param request
     * @return
     */
    @Override
    public List<TravelOrHotelPoiInfo> recommendTravelOrHotel(JSONObject jsodLog, CrossRecRequest request, CrossRecResponse response){

        long startTime = System.currentTimeMillis();
        List<CrossTravelOrderInfo> travelOrderInfo = request.getTravelOrderInfo();
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        if(CollectionUtils.isEmpty(travelOrderInfo)){
            logger.warn(AvailField.TRAVEL_ORDERINFO_WARNING + request);
            return poiList;
        }

        //获取旅游信息
        PoiInfo poiInfo = travelOrderInfo.get(0).getPoiids().get(0);
        int cityid = poiInfo.getCityId();
        double lat = poiInfo.getLatitude();
        double lng = poiInfo.getLongitude();

        List<TravelOrHotelPoiInfo> nearPoiList = nearbyTravelRecommend.
                getNearbyTravelPoiList(cityid, lat, lng, response);
        poiList = TravelRecommendResult.recResult(jsodLog, nearPoiList, null, cityid, request, -1);
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(AvailField.TRAVEL_NEARBY_TRAVEL_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.TRAVEL_NEARBY_TRAVEL_TIME, costTime);
        return poiList;
    }
}
