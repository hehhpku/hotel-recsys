package com.meituan.hotel.rec.cross.impl.scene;

import com.meituan.hotel.rec.cross.*;
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
 * 推荐旅游出发地旅游
 * Created by zuolin on 15/11/16.
 */
public class TrafficDepartTravelScene extends SceneClass{
    private static final Logger logger = LoggerFactory.getLogger(TrafficDepartTravelScene.class);
    private NearbyTravelRecommend nearbyTravelRecommend = new NearbyTravelRecommend();
    /**
     * 根据交通订单的出发地城市进行旅游推荐
     * @param request
     * @return
     */
    @Override
    public List<TravelOrHotelPoiInfo> recommendTravelOrHotel(JSONObject jsodLog, CrossRecRequest request, CrossRecResponse response){

        long startTime = System.currentTimeMillis();
        int cityid;
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        try{
            cityid = request.getTicketOrderInfo().
                    get(0).getDepartureCityid();
        }catch (Exception e){
            logger.error(AvailField.CITY_EXPLAIN_EXCEPTION + e);
            long costTime = System.currentTimeMillis() - startTime;
            logger.info(AvailField.TRAFFIC_DEPART_TRAVEL_TIME + costTime + "ms");
            JMonitor.add(JMonitorKey.TRAFFIC_DEPART_TRAVEL_EXCEPTION);
            return poiList;
        }

        List<TravelOrHotelPoiInfo> nearPoiList = nearbyTravelRecommend.getNearbyTravelPoiList(cityid, response);
        poiList = TravelRecommendResult.recResult(jsodLog, nearPoiList, null, cityid, request, -1);
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(AvailField.TRAFFIC_DEPART_TRAVEL_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.TRAFFIC_DEPART_TRAVEL_TIME, costTime);
        return poiList;
    }
}
