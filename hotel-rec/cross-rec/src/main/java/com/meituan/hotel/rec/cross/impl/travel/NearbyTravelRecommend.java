package com.meituan.hotel.rec.cross.impl.travel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.meituan.cache.redisCluster.client.typeInterface.IMedis;
import com.meituan.hotel.rec.cross.*;
import com.meituan.hotel.rec.cross.impl.Util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * 获取定位城市位置附近的poi及导入poi的基本信息
 * @author zuolin02@meituan.com
 * @date 2015-10-27
 * @brief
 */
public class NearbyTravelRecommend {
    private static IMedis medisClient = MedisClient.getMedisClient().getInstance();
    private static final Logger logger = LoggerFactory.getLogger(NearbyTravelRecommend.class);

    /**
     * 有定位推荐时使用
     * @param cityId
     * @param lat
     * @param lng
     * @return
     */
    public List<TravelOrHotelPoiInfo> getNearbyTravelPoiList(int cityId, double lat, double lng, CrossRecResponse response) {
        response.setStrategy("T_" + AvailField.FIRST_RANK_STRATEGY);
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        if (cityId < 0) {
            logger.warn(AvailField.CITYID_VALUE_WARNING + cityId);
            return poiList;
        }
        // 从medis中，获取本城市的所有旅游POI
        String cityPoiKey = AvailClass.TRAVEL_CITY_POI_PREKEY + cityId;
        String cityPoiInfoString = medisClient.getString(cityPoiKey);
        //存储候选集
        List<TravelOrHotelPoiInfo> candidateList = new ArrayList<TravelOrHotelPoiInfo>();
        try {
            JSONArray jsonArray = JSON.parseArray(cityPoiInfoString);
            int numOfPoiInCity = jsonArray.size();
            // 根据城市下Poi的数目，计算距离限制
            if (numOfPoiInCity == 0) {
                logger.warn(AvailField.CITY_NOPOI_WARNING + cityId);
                return poiList;
            }
            // 遍历该城市的poi
            for (int k = 0; k < numOfPoiInCity; k++)
            {
                JSONArray jaPoiInfo = jsonArray.getJSONArray(k);
                long poiId = jaPoiInfo.getInteger(0);
                double poiLat   = jaPoiInfo.getDouble(1);
                double poiLng   = jaPoiInfo.getDouble(2);
                int consume = jaPoiInfo.getInteger(3);
                double lowPrice = jaPoiInfo.getDouble(4);
                double highPrice = jaPoiInfo.getDouble(5);
                int evaluateCnt = jaPoiInfo.getInteger(6);
                double evaluateScore = jaPoiInfo.getDouble(7);
                String lastEndDealDate = jaPoiInfo.getString(8);
                String star = jaPoiInfo.getString(9);
                int typeId = jaPoiInfo.getInteger(10);
                double distance = AvailClass.calDistanceAccurate(poiLng, poiLat, lng, lat);
                if(distance < AvailClass.MAX_TRAVEL_CITY_RADIUS){
                    PoiBaseAttribute poiBaseAttribute = new PoiBaseAttribute(consume, lowPrice, highPrice,
                            evaluateCnt, evaluateScore,lastEndDealDate, star);
                    poiBaseAttribute.setDistance(distance);
                    TravelOrHotelPoiInfo poiInfo = new TravelOrHotelPoiInfo(poiId, poiLat, poiLng, typeId, poiBaseAttribute);
                    candidateList.add(poiInfo);
                }
            }
        } catch (Exception e) {
            logger.error(AvailField.NRARBY_POI_EXCEPTION,e);
        }
        Collections.sort(candidateList);
        poiList = candidateList.subList(0,Math.min(candidateList.size(),AvailClass.MAX_REC_NUM));
        return poiList;
    }

    /**
     * @param cityId
     * @param lat
     * @param lng
     * @return
     */
    public List<TravelOrHotelPoiInfo> getNearbyTravelPoiList(int cityId, double lat, double lng) {

        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        if (cityId < 0) {
            logger.warn(AvailField.CITYID_VALUE_WARNING + cityId);
            return poiList;
        }
        // 从medis中，获取本城市的所有旅游POI
        String cityPoiKey = AvailClass.TRAVEL_CITY_POI_PREKEY + cityId;
        String cityPoiInfoString = medisClient.getString(cityPoiKey);
        //存储候选集
        List<TravelOrHotelPoiInfo> candidateList = new ArrayList<TravelOrHotelPoiInfo>();
        try {
            JSONArray jsonArray = JSON.parseArray(cityPoiInfoString);
            int numOfPoiInCity = jsonArray.size();

            if (numOfPoiInCity == 0) {
                logger.warn(AvailField.CITY_NOPOI_WARNING + cityId);
                return poiList;
            }
            // 遍历该城市的poi
            for (int k = 0; k < numOfPoiInCity; k++)
            {
                JSONArray jaPoiInfo = jsonArray.getJSONArray(k);
                long poiId = jaPoiInfo.getInteger(0);
                double poiLat   = jaPoiInfo.getDouble(1);
                double poiLng   = jaPoiInfo.getDouble(2);
                int consume = jaPoiInfo.getInteger(3);
                double lowPrice = jaPoiInfo.getDouble(4);
                double highPrice = jaPoiInfo.getDouble(5);
                int evaluateCnt = jaPoiInfo.getInteger(6);
                double evaluateScore = jaPoiInfo.getDouble(7);
                String lastEndDealDate = jaPoiInfo.getString(8);
                String star = jaPoiInfo.getString(9);
                int typeId = jaPoiInfo.getInteger(10);
                double distance = AvailClass.calDistanceAccurate(poiLng, poiLat, lng, lat);
                if(distance < AvailClass.MAX_TRAVEL_CITY_RADIUS){
                    PoiBaseAttribute poiBaseAttribute = new PoiBaseAttribute(consume, lowPrice, highPrice,
                            evaluateCnt, evaluateScore,lastEndDealDate, star);
                    poiBaseAttribute.setDistance(distance);
                    TravelOrHotelPoiInfo poiInfo = new TravelOrHotelPoiInfo(poiId, poiLat, poiLng, typeId, poiBaseAttribute);
                    candidateList.add(poiInfo);
                }
            }
        } catch (Exception e) {
            logger.error(AvailField.NRARBY_POI_EXCEPTION,e);
        }
        Collections.sort(candidateList);
        poiList = candidateList.subList(0,Math.min(candidateList.size(),AvailClass.MAX_REC_NUM));
        return poiList;
    }

    /**
     * 根据城市返回热门旅游单
     * @param cityId
     * @return
     */
    public List<TravelOrHotelPoiInfo> getNearbyTravelPoiList(int cityId, CrossRecResponse response) {
        response.setStrategy("T_" + AvailField.FIRST_RANK_STRATEGY);
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        if (cityId < 0) {
            logger.warn(AvailField.CITYID_VALUE_WARNING + cityId);
            return poiList;
        }
        // 从medis中，获取本城市的旅游POI
        String cityPoiKey = AvailClass.TRAVEL_CITY_POI_PREKEY + cityId;
        String cityPoiInfoString = medisClient.getString(cityPoiKey);
        try {
            JSONArray jsonArray = JSON.parseArray(cityPoiInfoString);
            int numOfPoiInCity = jsonArray.size();

            if (numOfPoiInCity == 0) {
                logger.warn(AvailField.CITY_NOPOI_WARNING);
                return poiList;
            }
            // 遍历该城市的前100热门poi
            for (int k = 0; k < Math.min(numOfPoiInCity, AvailClass.MAX_REC_NUM); k++)
            {
                JSONArray jaPoiInfo = jsonArray.getJSONArray(k);
                long poiId = jaPoiInfo.getInteger(0);
                double poiLat   = jaPoiInfo.getDouble(1);
                double poiLng   = jaPoiInfo.getDouble(2);
                int consume = jaPoiInfo.getInteger(3);
                double lowPrice = jaPoiInfo.getDouble(4);
                double highPrice = jaPoiInfo.getDouble(5);
                int evaluateCnt = jaPoiInfo.getInteger(6);
                double evaluateScore = jaPoiInfo.getDouble(7);
                String lastEndDealDate = jaPoiInfo.getString(8);
                String star = jaPoiInfo.getString(9);
                int typeId = jaPoiInfo.getInteger(10);
                PoiBaseAttribute poiBaseAttribute = new PoiBaseAttribute(consume, lowPrice, highPrice,
                            evaluateCnt, evaluateScore,lastEndDealDate, star);
                TravelOrHotelPoiInfo poiInfo = new TravelOrHotelPoiInfo(poiId, poiLat, poiLng, typeId, poiBaseAttribute);
                poiList.add(poiInfo);
            }
        } catch (Exception e) {
            logger.error(AvailField.HOTPOI_PARSE_EXCEPTION,e);
        }
        return poiList;
    }

    /**
     * 酒店-旅游的CF推荐
     * @param hotelPoiId
     * @param response
     * @return
     */
    public List<TravelOrHotelPoiInfo> getTravelCFFromHotel(long hotelPoiId, double lat, double lng, CrossRecResponse response, String strategy, int localOrRemote){
        response.setStrategy("T_" + strategy);
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();

        //medis中获取与酒店相关的旅游poi
        String hotelPoiKey = "";
        if (AvailField.TRAVEL_CF_HOTEL_VIEW_STRATEGY.equals(strategy)){
            hotelPoiKey = AvailClass.TARVEL_CORR_HOTEL_PREKEY + hotelPoiId;
        }else if (AvailField.TRAVEL_CF_HOTEL_PAY_STRATEGY.equals(strategy)){
            hotelPoiKey = AvailClass.TARVEL_CORR_HOTEL_PAY_PREKEY + hotelPoiId;
        }else {
            //0:本地用户
            if(localOrRemote == 0){
                hotelPoiKey = AvailClass.TARVEL_CORR_HOTEL_LOCAL_PREKEY + hotelPoiId;
            }else{
                hotelPoiKey = AvailClass.TARVEL_CORR_HOTEL_REMOTE_PREKEY + hotelPoiId;
            }
        }
        String hotelPoiInfoString = medisClient.getString(hotelPoiKey);
        try {
            JSONArray jsonArray = JSON.parseArray(hotelPoiInfoString);
            int numOfPoiByHotel = jsonArray.size();

            if (numOfPoiByHotel == 0) {
                logger.warn(AvailField.HOTEL_NOCORR_TRAVEL_WARNING, hotelPoiId);
                return poiList;
            }

            for (int k = 0; k < Math.min(numOfPoiByHotel, AvailClass.MAX_CF_NUM); k++)
            {
                JSONArray jaInfo = jsonArray.getJSONArray(k);
                long poiId = jaInfo.getInteger(0);
                double poiLat   = jaInfo.getDouble(1);
                double poiLng   = jaInfo.getDouble(2);
                int consume = jaInfo.getInteger(3);
                String lastEndDealDate = jaInfo.getString(4);
                String star = jaInfo.getString(5);
                int typeId = jaInfo.getInteger(6);
                double corr = jaInfo.getDouble(7);
                double distance = AvailClass.calDistanceAccurate(poiLng, poiLat, lng, lat);
                PoiBaseAttribute poiBaseAttribute = new PoiBaseAttribute();
                poiBaseAttribute.setDistance(distance);
                poiBaseAttribute.setConsume(consume);
                poiBaseAttribute.setStar(star);
                poiBaseAttribute.setHotelTravelCorrScore(corr);
                poiBaseAttribute.setLastDealDate(lastEndDealDate);
                TravelOrHotelPoiInfo poiInfo = new TravelOrHotelPoiInfo(poiId, poiLat, poiLng, typeId, poiBaseAttribute);
                poiList.add(poiInfo);
            }
        }catch (Exception e){
            logger.error(AvailField.MEDIS_READ_EXCEPTION,e);
        }
        return poiList;
    }

    public static void main(String args[]){
        NearbyTravelRecommend nearbyTravelRecommend = new NearbyTravelRecommend();
        List<TravelOrHotelPoiInfo> retList = nearbyTravelRecommend.getTravelCFFromHotel(1272804,30.711539,111.295087,new CrossRecResponse(),AvailField.TRAVEL_CF_HOTEL_VIEW_STRATEGY,-1);
        for(TravelOrHotelPoiInfo poiInfo : retList){
            System.out.println(poiInfo.toString()  + "\t" + poiInfo.getPoiBaseAttribute().getConsume() + "\t" + poiInfo.getPoiBaseAttribute().getDistance());
        }
    }
}
