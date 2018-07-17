package com.meituan.hotel.rec.cross.impl.scene;


import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import com.meituan.hotel.rec.cross.impl.Util.AvailClass;
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
 * 推荐交通出发地旅游
 * Created by zuolin on 15/11/16.
 */
public class TrafficDepartHotelScene extends SceneClass{

    private static final Logger logger = LoggerFactory.getLogger(TrafficDepartHotelScene.class);
    private NearbyHotelRecommend nearbyHotelRecommend = new NearbyHotelRecommend();
    /**
     * 根据交通订单的出发地城市进行酒店推荐
     * @param request
     * @return
     */
    @Override
    public List<TravelOrHotelPoiInfo> recommendTravelOrHotel(JSONObject jsodLog, CrossRecRequest request, CrossRecResponse response) {
        long startTime = System.currentTimeMillis();
        int cityid;
        long userid ;
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        try{
            cityid = request.getTicketOrderInfo().
                    get(0).getDepartureCityid();
            userid = request.getUserId();
        }catch (Exception e){
            logger.error(AvailField.CITY_EXPLAIN_EXCEPTION + e);
            long costTime = System.currentTimeMillis() - startTime;
            logger.info(AvailField.TRAFFIC_DEPART_HOTEL_TIME + costTime + "ms");
            JMonitor.add(JMonitorKey.TRAFFIC_DEPART_HOTEL_TIME, costTime);
            return poiList;
        }
        List<TravelOrHotelPoiInfo> candiante = AvailClass.historyRecommend.getNearbyHotelPoiList(userid, cityid, response);
        List<TravelOrHotelPoiInfo> nearPoiList;
        poiList = candiante;
        try{
            if (!CollectionUtils.isEmpty(candiante)){
                for (TravelOrHotelPoiInfo poi : candiante){
                    double lat = poi.getLat();
                    double lng = poi.getLng();
                    nearPoiList = nearbyHotelRecommend.getNearbyHotelPoiList(cityid,lat,lng,response);
                    if (!CollectionUtils.isEmpty(nearPoiList)){
                        for (TravelOrHotelPoiInfo poiInfo : nearPoiList){
                            if (poiList.size() >= AvailClass.FACT_RETURN_NUM){
                                break;
                            }
                            if (AvailClass.isExist(poiList, poiInfo)){
                                poiList.add(poiInfo);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error(AvailField.MEDIS_READ_EXCEPTION, e);
        }
        poiList = HotelRecommendResult.recResult(jsodLog, poiList, cityid);
        long costTime = System.currentTimeMillis() - startTime;
        logger.info(AvailField.TRAFFIC_DEPART_HOTEL_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.TRAFFIC_DEPART_HOTEL_TIME, costTime);
        return poiList;
    }

}
