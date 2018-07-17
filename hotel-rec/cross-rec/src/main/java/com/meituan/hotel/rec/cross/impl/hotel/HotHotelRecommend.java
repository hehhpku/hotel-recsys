package com.meituan.hotel.rec.cross.impl.hotel;

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
 * 根据城市热门推荐酒店
 * Created by zuolin on 15/11/25.
 */
public class HotHotelRecommend {
    private static IMedis medis = MedisClient.getMedisClient().getInstance();
    private static final Logger logger = LoggerFactory.getLogger(HotHotelRecommend.class);

    /**
     * 根据cityid获取城市前100酒店poi作为候选集
     * @param cityId
     * @return
     */
    public List<TravelOrHotelPoiInfo> getHotHotelPoiList(int cityId){
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
                long poiid = jsonArray.getInteger(i);
                TravelOrHotelPoiInfo poiInfo = new TravelOrHotelPoiInfo();
                poiInfo.setPoiId(poiid);
                poiList.add(poiInfo);
            }
        }catch (Exception e){
            logger.error(AvailField.HOTPOI_MEDIS_EXCEPTION,e);
        }
        return poiList;
    }

    public static void main(String args[]){
        System.out.println(new HotHotelRecommend().getHotHotelPoiList(0));
    }

}
