package com.meituan.hotel.rec.service.recall.implement;

import com.alibaba.fastjson.*;
import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.constants.MedisClient;
import com.meituan.hotel.rec.service.recall.IRecaller;
import com.meituan.hotel.rec.service.recall.common.RecallRequest;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.*;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.util.CollectionUtil;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hehuihui on 3/15/16
 * 召回城市下热门酒店
 * 性能：10ms以内
 */
@Service("city-hot-poi-recall-service")
public class CityHotPoiRecaller implements IRecaller {
    public static final String STRATEGY = "cityHotPoiRecallStrategy";
    public static final int HOT_RECALL_NUM = 200;
    private static final Logger logger = RecUtils.getLogger( CityHotPoiRecaller.class.getSimpleName());

    @Override
    public RecResponse recall(RecallRequest request, JSONObject joLog) {
        long start = System.currentTimeMillis();
        List<PoiBasicEntry> hotPoiInfoList = new ArrayList<PoiBasicEntry>();
        //获取候选城市
        Set<Integer> cityIdSet = new HashSet<Integer>();
        cityIdSet.add(request.getChannelCityId());
        if (CollectionUtils.isNotEmpty(request.getPoiCityIdList())) {
            cityIdSet.addAll(request.getPoiCityIdList());
        }

        Location requestLocation = RecDistanceUtils.getNullLocation();
        boolean isValidLocation = false;
        if (RecDistanceUtils.isValidLocation(request.getRequestLocation())) {
            requestLocation = request.getRequestLocation();
            isValidLocation = true;
        }
        double poiLat = requestLocation.getLattitude();
        double poiLng = requestLocation.getLongitude();
        JSONArray jaPoiArray;

        RecResponse response = new RecResponse();
        Set<Integer> poiSet = new HashSet<Integer>(request.getBlackPoiSet()); //去重
        try{
            List<String> keys = new ArrayList<String>();
            for (int cityId: cityIdSet) {
                keys.add(MedisClient.CITY_HOT_POI_PREFIX_NEW + cityId);
            }
            Map<String, String> cityPoiListString = MedisClient.medisClient.multiGetString(keys);
            //解析城市下的热门poi，计算与用户、当前poi的距离
            for (int cityId: cityIdSet){
                String poiListString = cityPoiListString.get(MedisClient.CITY_HOT_POI_PREFIX_NEW + cityId);
                if (poiListString != null){
                    JSONArray jaCityHotPoi = JSON.parseArray(poiListString);
                    for (int k = 0; k < jaCityHotPoi.size(); k++) {
                        jaPoiArray = jaCityHotPoi.getJSONArray(k);
                        int poiId = jaPoiArray.getInteger(MedisClient.CITY_HOT_POI_ID);
                        if (poiSet.contains(poiId)){
                            continue;
                        }
                        double lat = jaPoiArray.getDouble(MedisClient.CITY_HOT_POI_LAT);
                        double lng = jaPoiArray.getDouble(MedisClient.CITY_HOT_POI_LNG);
                        double distanceToPoi = RecDistanceUtils.calDistance(lng, lat, poiLng, poiLat);
                        PoiBasicEntry e = new PoiBasicEntry(poiId, distanceToPoi);
                        hotPoiInfoList.add(e);
                        poiSet.add(poiId);
                    }
                }
            }

            //利用与当前poi的距离排序，截取前HOT_RECALL_NUM个返回
            if (isValidLocation) {
                Collections.sort(hotPoiInfoList, new Comparator<PoiBasicEntry>() {
                    @Override
                    public int compare(PoiBasicEntry o1, PoiBasicEntry o2) {
                        return Double.valueOf(o1.getDistanceToRequest()).compareTo(o2.getDistanceToRequest());
                    }
                });
            }
            hotPoiInfoList = CollectionUtil.subList(hotPoiInfoList, 0, HOT_RECALL_NUM);
            response.setServiceStatus(RecServiceStatus.OK);
        }catch (Exception e){
            response.setServiceStatus(RecServiceStatus.ERROR);
            logger.error(RecUtils.getErrorString("CityHotPoiRecaller"), e);
        }

        List<PoiEntry> poiEntryList = new ArrayList<PoiEntry>(hotPoiInfoList.size());
        for (PoiBasicEntry e: hotPoiInfoList){
            PoiEntry poiEntry = new PoiEntry(e);
            poiEntry.setSource(PoiSource.CITY_HOT);
            poiEntryList.add(poiEntry);

        }
        response.setPoiEntryList(poiEntryList);
        response.setStrategy(STRATEGY);

        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.CITY_HOT_POI_RECALL_TIME, timeCost);
        return response;
    }
}
