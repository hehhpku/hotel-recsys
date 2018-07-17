package com.meituan.hotel.rec.cross.impl.hotel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meituan.cache.redisCluster.client.typeInterface.IMedis;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import com.meituan.hotel.rec.cross.impl.Util.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * 推荐定位城市附近的poi信息
 * Created by zuolin on 15/11/24.
 */
public class NearbyHotelRecommend {
    private static IMedis medis = MedisClient.getMedisClient().getInstance();
    private static final Logger logger = LoggerFactory.getLogger(NearbyHotelRecommend.class);

    /**
     * 根据定位的经纬度获取附近的酒店
     * @param cityId
     * @param lat
     * @param lng
     * @return
     */
    public List<TravelOrHotelPoiInfo> getNearbyHotelPoiList(int cityId, double lat, double lng, CrossRecResponse response){
        response.setStrategy("H_" + AvailField.FIRST_RANK_STRATEGY);
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        if (cityId  < 0){
            logger.warn(AvailField.CITY_EXPLAIN_EXCEPTION + cityId);
            return poiList;
        }
        // 从medis中，获取本城市的所有酒店POI
        String cityPoiKey = AvailClass.HOTEL_CITY_POI_PREKEY + cityId;
        String cityPoiInfoString = medis.getString(cityPoiKey);

        //存储候选集
        List<TravelOrHotelPoiInfo> candidateList = new ArrayList<TravelOrHotelPoiInfo>();
        try{
            JSONArray jsonArray = JSON.parseArray(cityPoiInfoString);
            int numOfPoiInCity = jsonArray.size();
            if (numOfPoiInCity == 0){
                logger.warn(AvailField.CITY_NOPOI_WARNING + cityId);
                return poiList;
            }
            //访问城市下的酒店poi
            for (int i = 0 ; i < numOfPoiInCity ;i++){
                JSONArray jaPoiInfo = jsonArray.getJSONArray(i);
                int poiid = jaPoiInfo.getInteger(0);
                double poiLat = jaPoiInfo.getDouble(1);
                double poiLng = jaPoiInfo.getDouble(2);
                double distance = AvailClass.calDistanceAccurate(poiLng, poiLat, lng, lat);
                if (distance < AvailClass.MAX_HOTEL_CITY_RADIUS){
                    TravelOrHotelPoiInfo poiInfo = new TravelOrHotelPoiInfo();
                    poiInfo.setLat(poiLat);
                    poiInfo.setLng(poiLng);
                    poiInfo.setPoiId(poiid);
                    poiInfo.getPoiBaseAttribute().setDistance(distance);
                    candidateList.add(poiInfo);
                }

            }

        }catch (Exception e){
            logger.error(AvailField.NRARBY_POI_EXCEPTION,e);
        }
        Collections.sort(candidateList);
        poiList = candidateList.subList(0,Math.min(candidateList.size(),AvailClass.MAX_REC_NUM));
        //导入poi的基本信息
        poiList = loadPoiBaseAttribute(poiList);
        return poiList;
    }

    /**
     * 根据cityid获取城市前100酒店poi作为候选集
     * @param cityId
     * @return
     */
    public List<TravelOrHotelPoiInfo> getNearbyHotelPoiList( int cityId, CrossRecResponse response){
        response.setStrategy("H_" + AvailField.FIRST_RANK_STRATEGY);
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        if (cityId < 0){
            logger.warn(AvailField.CITY_EXPLAIN_EXCEPTION + cityId);
            return poiList;
        }

        String cityPoiKey = AvailClass.HOTEL_CITY_HOT_POI_KEY + cityId;
        String cityPoiInfoString = medis.getString(cityPoiKey);

        try{
            JSONArray jsonArray = JSON.parseArray(cityPoiInfoString);
            int numOfPoi = jsonArray.size();

            if (numOfPoi == 0){
                logger.warn(AvailField.CITY_NOPOI_WARNING + cityId);
                return poiList;
            }

            for (int i = 0; i< Math.min(AvailClass.MAX_REC_NUM, numOfPoi); i++){
                int poiid = jsonArray.getInteger(i);
                TravelOrHotelPoiInfo poiInfo = new TravelOrHotelPoiInfo();
                poiInfo.setPoiId(poiid);
                poiList.add(poiInfo);
            }
        }catch (Exception e){
            logger.error(AvailField.HOTPOI_MEDIS_EXCEPTION,e);
        }
        //导入poi的基本信息
        poiList = loadPoiBaseAttribute(poiList);
        return poiList;
    }

    /**
     * 导入酒店poi的基本信息（销量，评价，价格等）
     * @param poiList
     * @return
     */
    public List<TravelOrHotelPoiInfo> loadPoiBaseAttribute(List<TravelOrHotelPoiInfo> poiList){
        if(CollectionUtils.isEmpty(poiList)){
            logger.warn(AvailField.POILIST_EMPTY_WARNING);
            return poiList;
        }
        //根据poi下的deal信息来获取poi的相关信息
        List<String> poiDealKey = new ArrayList<String>();
        for (TravelOrHotelPoiInfo poiInfo: poiList){
            long poiId = poiInfo.getPoiId();
            poiDealKey.add(AvailClass.HOTEL_DEAL_INFO_KEY + poiId);
        }
        //批量获取poi下的deal信息
        Map<String,String> dealMap = medis.multiGetString(poiDealKey);
        for (TravelOrHotelPoiInfo poiInfo : poiList){
            try{
                long poiId = poiInfo.getPoiId();
                JSONObject dealInfo = JSON.parseObject(dealMap.get(AvailClass.HOTEL_DEAL_INFO_KEY + poiId));
                int sales = dealInfo.getInteger("sales");
                JSONArray jsonArray = dealInfo.getJSONArray("deals");
                double lowPrice = Double.MAX_VALUE;
                double highPrice = Double.MIN_VALUE;
                double evaluateScore = 0;
                int dealNum = jsonArray.size();
                if (dealNum == 0){
                    logger.warn(AvailField.POILIST_EMPTY_WARNING + poiId);
                    continue;
                }
                for (int k = 0; k < dealNum; k++){
                    JSONArray dealsArray = jsonArray.getJSONArray(k);
                    double price = dealsArray.getDouble(1);
                    evaluateScore += dealsArray.getDouble(5);
                    if (price > highPrice){
                        highPrice = price;
                    }
                    if (price < lowPrice){
                        lowPrice = price;
                    }
                }
                poiInfo.getPoiBaseAttribute().setEvaluateScore(evaluateScore / dealNum);
                poiInfo.getPoiBaseAttribute().setHighPrice(highPrice);
                poiInfo.getPoiBaseAttribute().setLowPrice(lowPrice);
                poiInfo.getPoiBaseAttribute().setConsume(sales);
            }catch (Exception e){
                logger.error(AvailField.MEDIS_READ_EXCEPTION,e);
            }
        }
        return poiList;
    }

    public static void main(String[] args) {
        NearbyHotelRecommend nearbyHotelRecommend = new NearbyHotelRecommend();
        List<TravelOrHotelPoiInfo> s = nearbyHotelRecommend.getNearbyHotelPoiList(818, 39.497502, 116.89, new CrossRecResponse());
        for(TravelOrHotelPoiInfo poiInfo : s){
            System.out.println(poiInfo.toString() + "\t"  + poiInfo.getPoiBaseAttribute().getDistance());
        }
    }
}
