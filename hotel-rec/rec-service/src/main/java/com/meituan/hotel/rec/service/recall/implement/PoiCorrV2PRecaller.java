package com.meituan.hotel.rec.service.recall.implement;

import com.alibaba.fastjson.JSONArray;
import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.constants.*;
import com.meituan.hotel.rec.service.recall.IRecaller;
import com.meituan.hotel.rec.service.recall.common.RecallRequest;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.jmonitor.JMonitor;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zuolin02 and hehuihui on 3/15/16
 */
@Service("poi-corr-v2p-recall-service")
public class PoiCorrV2PRecaller implements IRecaller{
    private static final Logger logger = RecUtils.getLogger(PoiCorrV2PRecaller.class.getSimpleName());
    public static final String STRATEGY = "poiCorrV2PRecallStrategy";

    @Override
    public RecResponse recall(RecallRequest request, JSONObject joLog) {
        long start = System.currentTimeMillis();

        if (CollectionUtils.isEmpty(request.getPoiOnShow())){
            RecResponse response = RecResponse.getErrorResponse();
            response.setStrategy(STRATEGY);
            return response;
        }

        int poiId = request.getPoiOnShow().get(0);
        Location reqLocation = request.getRequestLocation();
        double poiLat = reqLocation.getLattitude();
        double poiLng = reqLocation.getLongitude();

        List<PoiEntry> poiEntryList = readMedisData(poiId, poiLng, poiLat,
                MedisClient.POI_V2P_CORR_PREFIX_KEY);

        for (PoiEntry e: poiEntryList){
            e.setSource(PoiSource.CORR_V2P);
        }

        RecResponse response = new RecResponse();

        // 召回结果太少时，丢弃这些结果，采用其他召回策略（城市下poi的召回策略）
        if (poiEntryList.size() < 3){
            response.setServiceStatus(RecServiceStatus.DEGRADE);
        } else {
            response.setServiceStatus(RecServiceStatus.OK);
        }
        response.setPoiEntryList(poiEntryList);
        response.setStrategy(STRATEGY);

        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.CITY_POI_RECALL_CORR_TIME, timeCost);

        return response;
    }

    /**
     * 从medis中读取与当前poi相关联的poi
     * @param poiId
     * @param poiLng
     * @param poiLat
     * @param medisPreKey
     * @return
     */
    public static List<PoiEntry> readMedisData(int poiId, double poiLng, double poiLat, String medisPreKey) {
        List<PoiEntry> corrPoiInfoList = new ArrayList<PoiEntry>();

        String corrMedisKey = medisPreKey + poiId;
        String corrPoiString = MedisClient.medisClient.getString(corrMedisKey);
        if (corrPoiString == null) {
            logger.warn(RecUtils.getWarnString("poi=" + poiId +",hasNoCORR"));
            return corrPoiInfoList;
        }
        JSONArray jsonCorrArray = JSONArray.parseArray(corrPoiString);
        try {
            int corrPoiNum = jsonCorrArray.size();
            for (int k = 0; k < corrPoiNum; k++) {
                JSONArray poiInfo = jsonCorrArray.getJSONArray(k);
                int poiTemp = poiInfo.getInteger(0);
                double corrScore = poiInfo.getDouble(1);
                double lat = poiInfo.getDouble(2);
                double lng = poiInfo.getDouble(3);

                double distanceToPoi = RecDistanceUtils.calDistance(lng, lat, poiLng, poiLat);
                PoiEntry poiEntry = new PoiEntry();
                poiEntry.setPoiId(poiTemp);
                poiEntry.setCorrScore(corrScore);
                poiEntry.setDistanceToRequest(distanceToPoi);
                corrPoiInfoList.add(poiEntry);
            }
        } catch (Exception e) {
            logger.error(RecUtils.getErrorString("readMedisData()"), e);
        }
        return corrPoiInfoList;
    }

    /**
     * 根据列表获取关联的poi
     * @param poiIds
     * @param poiLng
     * @param poiLat
     * @param medisPreKey
     * @return
     */
    public static List<PoiEntry> getPoiRelationItems(List<Integer> poiIds, double poiLng, double poiLat, String medisPreKey) {
        List<PoiEntry> corrPoiInfoList;

        List<String> keys = new ArrayList<String>();
        for (int poiId: poiIds) {
            keys.add( medisPreKey + poiId );
        }
        Map<String, String> map = MedisClient.medisClient.multiGetString(keys);
        Map<Integer, PoiEntry> poiEntryMap = new HashMap<Integer, PoiEntry>();
        for (int poiId: poiIds) {
            String key = medisPreKey + poiId;
            String value = map.get(key);
            if (value == null) {
                continue;
            }
            JSONArray jsonCorrArray = JSONArray.parseArray(value);
            try {
                int corrPoiNum = jsonCorrArray.size();
                for (int k = 0; k < corrPoiNum; k++) {
                    JSONArray poiInfo = jsonCorrArray.getJSONArray(k);
                    int poiTemp = poiInfo.getInteger(0);
                    double corrScore = poiInfo.getDouble(1);
                    //已经添加该poi则更新poi的关联分，取关联最高分作为关联分
                    if (poiEntryMap.containsKey(poiTemp)){
                        if (poiEntryMap.get(poiTemp).getCorrScore() < corrScore) {
                            poiEntryMap.get(poiTemp).setCorrScore(corrScore);
                        }
                        continue;
                    }
                    double lat = poiInfo.getDouble(2);
                    double lng = poiInfo.getDouble(3);

                    double distanceToPoi = RecDistanceUtils.calDistance(lng, lat, poiLng, poiLat);
                    PoiEntry poiEntry = new PoiEntry();
                    poiEntry.setPoiId(poiTemp);
                    poiEntry.setCorrScore(corrScore);
                    poiEntry.setDistanceToRequest(distanceToPoi);
                    poiEntryMap.put(poiTemp, poiEntry);
                }
            } catch (Exception e) {
                logger.error(RecUtils.getErrorString("readMedisData()"), e);
            }
        }
        corrPoiInfoList = new ArrayList<PoiEntry>(poiEntryMap.values());
        return corrPoiInfoList;
    }



}
