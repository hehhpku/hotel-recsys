package com.meituan.hotel.rec.cross.impl.hotel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.meituan.cache.redisCluster.client.typeInterface.IMedis;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import com.meituan.hotel.rec.cross.impl.Util.AvailClass;
import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.MedisClient;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zuolin on 16/1/11.
 */
public class BasedOnHistoryRecommend {
    private static IMedis medis = MedisClient.getMedisClient().getInstance();
    private static final Logger logger = LoggerFactory.getLogger(BasedOnHistoryRecommend.class);

    /**
     * 从用户的历史记录中获取酒店poi
     * @param userid
     * @param cityId
     * @param response
     * @return
     */
    public List<TravelOrHotelPoiInfo> getNearbyHotelPoiList(long userid, int cityId, CrossRecResponse response){
        response.setStrategy("H_" + AvailField.FIRST_RANK_STRATEGY);
        List<TravelOrHotelPoiInfo> poiList = new ArrayList<TravelOrHotelPoiInfo>();
        if (cityId  < 0){
            logger.warn(AvailField.CITY_EXPLAIN_EXCEPTION + cityId);
            return poiList;
        }
        //获取用户历史购买过的酒店poi
        String useridPayKey = AvailClass.USERID_HOTEL_POI_KEY + userid;
        String useridPayString = medis.getString(useridPayKey);

        try {
            JSONArray jsonArray = JSON.parseArray(useridPayString);
            int numOfPayPoi = jsonArray.size();
            if (numOfPayPoi == 0) {
                logger.warn(AvailField.CITY_NOPOI_WARNING + cityId);
                return poiList;
            }
            //访问用户在同城市购买的poi
            for (int i = 0 ; i < numOfPayPoi ;i++){
                JSONArray jaPoiInfo = jsonArray.getJSONArray(i);
                int poiid = jaPoiInfo.getInteger(0);
                int city = jaPoiInfo.getInteger(1);
                double poiLat = jaPoiInfo.getDouble(2);
                double poiLng = jaPoiInfo.getDouble(3);
                if (city == cityId){
                    TravelOrHotelPoiInfo poiInfo = new TravelOrHotelPoiInfo();
                    poiInfo.setPoiId(poiid);
                    poiInfo.setLat(poiLat);
                    poiInfo.setLng(poiLng);
                    poiList.add(poiInfo);
                }
            }
        }catch (Exception e){
            logger.error(AvailField.NRARBY_POI_EXCEPTION,e);
        }
        return poiList;
    }
}
