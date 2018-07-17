package com.meituan.hotel.rec.cross.impl.hotel;

import com.meituan.hotel.rec.cross.impl.Util.AvailClass;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zuolin on 15/11/26.
 */
public class HotelRecommendResult {
    /**
     * 返回推荐结果，不足加热门
     * @param poiList
     * @param cityid
     * @return
     */
    public static List<TravelOrHotelPoiInfo> recResult(JSONObject jsodLog,List<TravelOrHotelPoiInfo> poiList, int cityid ){
        poiList = AvailClass.hotelPoiSort.sortHotelPoi(jsodLog, poiList);
        HashSet<Long> uniqPoiSet = new HashSet<Long>();
        List<TravelOrHotelPoiInfo> uniqPoiList = new ArrayList<TravelOrHotelPoiInfo>();
        if (poiList != null && !poiList.isEmpty()) {
            for (TravelOrHotelPoiInfo poi : poiList) {
                long poiId = poi.getPoiId();
                if (!uniqPoiSet.contains(poiId)) {
                    uniqPoiList.add(poi);
                    uniqPoiSet.add(poiId);
                }
                if (uniqPoiList.size() >= AvailClass.FACT_RETURN_NUM) {
                    break;
                }
            }
        }
        poiList = uniqPoiList;

        int currentNum = poiList.size();
        //数目不够时以热门补充
        if (currentNum < AvailClass.FACT_RETURN_NUM){
            List<TravelOrHotelPoiInfo> hotTravelList = AvailClass.hotHotelRecommend.getHotHotelPoiList(cityid);
            for (TravelOrHotelPoiInfo poi : hotTravelList){
                if (!uniqPoiSet.contains(poi.getPoiId())){
                    poiList.add(poi);
                    uniqPoiSet.add(poi.getPoiId());
                    currentNum += 1;
                }
                if (currentNum >= AvailClass.FACT_RETURN_NUM){
                    break;
                }
            }
        }
        return poiList;
    }
}
