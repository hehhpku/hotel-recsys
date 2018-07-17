package com.meituan.hotel.rec.cross.impl.travel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.meituan.cache.redisCluster.client.typeInterface.IMedis;
import com.meituan.hotel.rec.cross.impl.Util.AvailClass;
import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.MedisClient;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * 推荐城市下的热门poi
 * @author zuolin02@meituan.com
 * @date 2015-10-27
 * @brief
 */


public class HotTravelRecommend {

    private static IMedis medisClient = MedisClient.getMedisClient().getInstance();
    private static Logger logger = LoggerFactory.getLogger(HotTravelRecommend.class);

    /**获取旅游的热门poi列表
     * @param cityId
     * @return
     */
    public List<TravelOrHotelPoiInfo> getHotTravelPoiList(int cityId) {
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        if (cityId <= 0) {
            logger.warn(AvailField.CITYID_VALUE_WARNING + cityId);
            return poiList;
        }
        String travelCityHotPoikey = AvailClass.TRAVEL_CITY_HOT_POI_KEY + cityId;
        String travelCityHotPoi = null;
        try{
            travelCityHotPoi = medisClient.getString(travelCityHotPoikey);
        }catch (Exception e){
            logger.error(AvailField.HOTPOI_MEDIS_EXCEPTION + e.toString());
        }
        try {
            JSONArray jsonArray = JSON.parseArray(travelCityHotPoi);
            // 该城市下没有Poi
            if ( jsonArray == null || jsonArray.size() <= 0) {
                logger.warn(AvailField.CITY_NOPOI_WARNING + cityId);
                return poiList;
            }
            int numOfHotPoi = Math.min(AvailClass.MAX_REC_NUM, jsonArray.size());
            for (int k = 0; k < numOfHotPoi; k++) {
                JSONArray jaPoiInfo = jsonArray.getJSONArray(k);
                long poiid = jaPoiInfo.getInteger(0);
                int consume = jaPoiInfo.getInteger(1);
                String lastDealDate = jaPoiInfo.getString(2);
                String star = jaPoiInfo.getString(3);
                int typeId = jaPoiInfo.getInteger(4);
                TravelOrHotelPoiInfo poiInfo = new TravelOrHotelPoiInfo();
                poiInfo.setPoiId(poiid);
                poiInfo.setTypeId(typeId);
                poiInfo.getPoiBaseAttribute().setConsume(consume);
                poiInfo.getPoiBaseAttribute().setLastDealDate(lastDealDate);
                poiInfo.getPoiBaseAttribute().setStar(star);
                poiList.add(poiInfo);
            }
        }catch(Exception e){
              logger.error(AvailField.HOTPOI_PARSE_EXCEPTION + e.toString());
        }
        return poiList;
    }
}

