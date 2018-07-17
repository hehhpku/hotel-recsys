package com.meituan.hotel.rec.service.recall.implement;

import com.google.common.collect.Lists;

import com.alibaba.fastjson.*;
import com.meituan.hotel.rec.data.*;
import com.meituan.hotel.rec.data.client.RecDataClient;
import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.constants.*;
import com.meituan.hotel.rec.service.recall.IRecaller;
import com.meituan.hotel.rec.service.recall.common.RecallRequest;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.*;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.util.CollectionUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by jiangweisen and hehuihui on 3/15/16
 */
@Service("city-poi-recall-service")
public class CityPoiRecalller implements IRecaller {
    @Autowired
    private RecDataClient recDataClient;

    public static final String STRATEGY = "cityPoiRecallStrategy";
    public static final int POI_RECALL_NUM = 200;

    private static final Logger logger = RecUtils.getLogger(CityPoiRecalller.class.getSimpleName());

    @Override
    public RecResponse recall(RecallRequest request, JSONObject joLog) {
        long startTime = System.currentTimeMillis();

        List<PoiEntry> poiEntryList = Lists.newArrayList();
        RecDataResponse recDataResponse = getPoiFromRecData(request);
        List<HotelPoiBasicInfo> poiInfoList = recDataResponse.getPoiInfoList();
        Set<Integer> blackSet = request.getBlackPoiSet(); //类似"黑名单"
        if (CollectionUtils.isNotEmpty(poiInfoList)) {
            for (HotelPoiBasicInfo poiInfo : poiInfoList) {
                if (blackSet.contains(poiInfo.getPoiId())) {
                    continue;
                }
                poiEntryList.add(new PoiEntry(poiInfo));
            }
        } else {
            poiEntryList = getPoiFromMedis(request);
        }
        for (PoiEntry e: poiEntryList){
            e.setSource(PoiSource.NEAR_BY);
        }

        RecResponse recResponse = new RecResponse();
        recResponse.setStrategy(STRATEGY);
        recResponse.setServiceStatus(RecServiceStatus.OK);
        recResponse.setPoiEntryList(poiEntryList);

        long costTime = System.currentTimeMillis() - startTime;
        JMonitor.add(JmonitorKey.CITY_POI_RECALL_TIME, costTime);
        return recResponse;
    }

    /**
     * 查询RecData服务，获取当前POI附近的poi
     * cityId < 0: 不根据城市id过滤poi
     * @param request
     * @return
     */
    public RecDataResponse getPoiFromRecData(RecallRequest request) {
        long startTime = System.currentTimeMillis();

        // 组装请求字段
        PoiNearLocationReq poiNearLocationReq = new PoiNearLocationReq();
        int cityId = request.getChannelCityId();
        //详情页推荐的位置是准确的！不需要城市id信息
        if (RecServiceType.POI_DETAIL_REC != request.getType() && cityId > 0) {
            poiNearLocationReq.setCityId(cityId);
        }
        poiNearLocationReq.setNum(POI_RECALL_NUM);
        poiNearLocationReq.setLatitude(request.getRequestLocation().getLattitude());
        poiNearLocationReq.setLongitude(request.getRequestLocation().getLongitude());

        RecDataRequest recDataRequest = new RecDataRequest();
        recDataRequest.setOption(RequestOption.GET_NEARBY_POI);
        recDataRequest.setPoiNearLocationReq(poiNearLocationReq);

        // 查询RecData服务
        RecDataResponse recDataResponse = new RecDataResponse();
        try {
            recDataResponse = recDataClient.getHotelPoiNearLocation(recDataRequest);
        } catch (Exception e) {
            logger.error(RecUtils.getErrorString("getPoiFromRecData()"), e);
            recDataResponse.setStatus(ResponseStatus.ERROR);
        }

        long costTime = System.currentTimeMillis() - startTime;
        JMonitor.add(JmonitorKey.GET_POI_FROM_RECDATA_TIME, costTime);
        return recDataResponse;
    }

    /**
     * 查询medis，获取当前POI附近的poi
     * @param request
     * @return
     */
    public List<PoiEntry> getPoiFromMedis(RecallRequest request) {
        long startTime = System.currentTimeMillis();

        List<PoiEntry> poiEntryList = Lists.newArrayList();
        List<PoiBasicEntry> poiBasicEntries = new ArrayList<PoiBasicEntry>();
        Set<Integer> blackSet = request.getBlackPoiSet();

        Set<Integer> cityIdSet = new HashSet<Integer>();
        cityIdSet.add(request.getChannelCityId());
        if (CollectionUtils.isNotEmpty(request.getPoiCityIdList())) {
            cityIdSet.addAll(request.getPoiCityIdList());
        }

        Location requestLocation = request.getRequestLocation();
        double poiLat = requestLocation.getLattitude();
        double poiLng = requestLocation.getLongitude();

        List<String> keyList = Lists.newArrayList();
        for (int cityId : cityIdSet) {
            keyList.add(MedisClient.CITY_POI_PREFIX_NEX + cityId);
        }
        Map<String, String> medisValueMap = MedisClient.medisClient.multiGetString(keyList);

        for (int cityId : cityIdSet) {
            try {
                String medisValue = medisValueMap.get(MedisClient.CITY_POI_PREFIX_NEX + cityId);
                if (StringUtils.isNotBlank(medisValue)) {
                    JSONArray jsonArray = JSON.parseArray(medisValue);
                    double maxRecallDistance = getRecallDistance(jsonArray.size());
                    JSONArray jsonItem;
                    for (int i = 0; i < jsonArray.size(); i++) {
                        jsonItem = jsonArray.getJSONArray(i);
                        int poiId = jsonItem.getInteger(0);
                        double lat = jsonItem.getDouble(1);
                        double lng = jsonItem.getDouble(2);

                        double distanceToReq = RecDistanceUtils.calDistance(lng, lat, poiLng, poiLat);
                        if (distanceToReq < maxRecallDistance){
                            PoiBasicEntry entry = new PoiBasicEntry(poiId, -1.0, distanceToReq);
                            poiBasicEntries.add(entry);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(RecUtils.getErrorString("getPoiFromMedis()"), e);
            }
        }

        Collections.sort(poiBasicEntries, new Comparator<PoiBasicEntry>() {
            @Override
            public int compare(PoiBasicEntry o1, PoiBasicEntry o2) {
                return Double.valueOf(o1.getDistanceToRequest()).compareTo(o2.getDistanceToRequest());
            }
        });
        for (PoiBasicEntry e: poiBasicEntries){
            if (blackSet.contains(e.getPoiId())){
                continue;
            }
            poiEntryList.add(new PoiEntry(e));
            if(poiEntryList.size() >= POI_RECALL_NUM){
                break;
            }
        }

        long costTime = System.currentTimeMillis() - startTime;
        JMonitor.add(JmonitorKey.GET_POI_FROM_MEDIS_TIME, costTime);

        return CollectionUtil.subList(poiEntryList, 0, POI_RECALL_NUM);
    }

    /**
     * 根据城市下poi的数量，设置召回距离，优化性能
     * @param numOfPoi poi的数量
     * @return
     */
    public static double getRecallDistance(int numOfPoi){
        if (numOfPoi < 4000)
            return 30000;
        else if (numOfPoi < 8000)
            return 20000;
        else
            return 10000;
    }


}
