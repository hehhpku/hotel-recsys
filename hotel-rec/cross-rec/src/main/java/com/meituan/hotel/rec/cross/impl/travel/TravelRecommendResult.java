package com.meituan.hotel.rec.cross.impl.travel;

import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.impl.Util.AvailClass;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by zuolin on 15/11/20.
 */
public class TravelRecommendResult {

    /**
     * 返回推荐结果，不足加热门
     * @param poiList
     * @param cityid
     * @return
     */
    public static List<TravelOrHotelPoiInfo> recResult(JSONObject jsodLog, List<TravelOrHotelPoiInfo> poiList,
                                                       List<TravelOrHotelPoiInfo> addList, int cityid ,
                                                CrossRecRequest request, int localOrRemote){

        poiList = AvailClass.travelPoiSort.sortTravelPoi(jsodLog, poiList, 1);
        HashSet<Long> uniqPoiSet = new HashSet<Long>();
        List<TravelOrHotelPoiInfo> uniqPoiList = new ArrayList<TravelOrHotelPoiInfo>();
        //map记录每个类别的poi出现的次数
        Map<Integer, Integer> typeNumMap = new HashMap<Integer, Integer>();
        if (poiList != null && !poiList.isEmpty()) {
            for (TravelOrHotelPoiInfo poi : poiList) {
                long poiId = poi.getPoiId();
                int typeId = poi.getTypeId();
                if (!uniqPoiSet.contains(poiId) && AvailClass.filter.filterPoi(poi,request)) {
                    if (isLegal(typeNumMap, typeId)){
                        uniqPoiList.add(poi);
                        uniqPoiSet.add(poiId);
                        if (!typeNumMap.containsKey(typeId)){
                            typeNumMap.put(typeId, 0);
                        }
                        int value = typeNumMap.get(typeId);
                        typeNumMap.put(typeId, value + 1);
                    }
                }
                if (uniqPoiList.size() >= AvailClass.FACT_RETURN_NUM) {
                    break;
                }
            }
        }

        int currentNum = uniqPoiList.size();

        if (addList != null) {
            if (currentNum < AvailClass.FACT_RETURN_NUM){
                if (localOrRemote != 1){     // 本地用户不考虑旅游景点的星级
                    for (TravelOrHotelPoiInfo poi : addList){
                        poi.getPoiBaseAttribute().setStar("-1");
                    }
                }
                addList = AvailClass.travelPoiSort.sortTravelPoi(jsodLog, addList, 1);
                for (TravelOrHotelPoiInfo poi : addList){
                    long poiId = poi.getPoiId();
                    int typeId = poi.getTypeId();
                    if (!uniqPoiSet.contains(poiId) && AvailClass.filter.filterPoi(poi,request)){
                        if (isLegal(typeNumMap, typeId)){
                            uniqPoiList.add(poi);
                            uniqPoiSet.add(poiId);
                            if (!typeNumMap.containsKey(typeId)){
                                typeNumMap.put(typeId, 0);
                            }
                            int value = typeNumMap.get(typeId);
                            typeNumMap.put(typeId, value + 1);
                            currentNum += 1;
                        }
                    }
                    if (currentNum >= AvailClass.FACT_RETURN_NUM){
                        break;
                    }
                }
            }
        }

        if (currentNum < AvailClass.FACT_RETURN_NUM){
            List<TravelOrHotelPoiInfo> hotTravelList = AvailClass.hotTravelRecommend.getHotTravelPoiList(cityid);
            for (TravelOrHotelPoiInfo poi : hotTravelList){
                long poiId = poi.getPoiId();
                int typeId = poi.getTypeId();
                if (!uniqPoiSet.contains(poiId) && AvailClass.filter.filterPoi(poi,request)){
                    if (isLegal(typeNumMap, typeId)){
                        uniqPoiList.add(poi);
                        uniqPoiSet.add(poiId);
                        if (!typeNumMap.containsKey(typeId)){
                            typeNumMap.put(typeId, 0);
                        }
                        int value = typeNumMap.get(typeId);
                        typeNumMap.put(typeId, value + 1);
                        currentNum += 1;
                    }
                }
                if (currentNum >= AvailClass.FACT_RETURN_NUM){
                    break;
                }
            }
        }

        poiList = uniqPoiList;
        return poiList;
    }

    /**
     * 判断是否可以添加某个品类的poi到推荐列表
     * @param typeNumMap
     * @param typeId
     * @return
     */
    public static boolean isLegal(Map<Integer, Integer> typeNumMap, int typeId){
        //poi为景点,在推荐列表中最多出现3次
        if(typeId == 368){
            if (typeNumMap.containsKey(typeId)){
                if (typeNumMap.get(typeId) < 3){
                    return true;
                }
                return false;
            }else{
                return true;
            }
        }else{
            if (typeNumMap.containsKey(typeId)){
                return false;
            }
            return true;
        }
    }

    /**
     * 计算非景点的个数
     * @param typeNumMap
     * @return
     */
    public static boolean sumNot368Num(Map<Integer, Integer> typeNumMap){
        int sum = 0 ;
        for (int index : typeNumMap.keySet()){
            if (index != 368){
                sum += typeNumMap.get(index);
            }
        }
        if (sum < 2){
            return true;
        }
        return false;
    }
}
