package com.meituan.hotel.rec.service.pack;

import com.google.common.collect.Maps;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.external.RecStidClient;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.RecServiceType;

import org.apache.commons.lang.math.RandomUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hehuihui on 3/24/16
 */
@Service("packer")
public class Packer {

    private static final Logger logger = RecUtils.getLogger(Packer.class.getSimpleName());

    @Autowired
    private RecStidClient recStidClient;

    public List<PoiEntry> pack(List<PoiEntry> poiEntryList, RecRequest request, JSONObject joLog){
        //增加ct_poi参数
        poiEntryList = addCtPoi(poiEntryList, request, joLog);

        try{
            //筛选推荐收集距离信息，兼容旧的
            if (RecServiceType.SELECT_REC == request.getServiceType()) {
                List<Map.Entry<Integer, List<Double>>> poiFeatureList = new ArrayList<Map.Entry<Integer, List<Double>>>();
                for (PoiEntry hotelPoiInfo : poiEntryList) {
                    int poiId = hotelPoiInfo.getPoiId();
                    List<Double> featureValueList = new ArrayList<Double>();
                    featureValueList.add(hotelPoiInfo.getDistanceToRequest());
                    poiFeatureList.add(Maps.immutableEntry(poiId, featureValueList));
                }
                joLog.put("poi2FeatureValue", RecUtils.entryListToString(poiFeatureList, ":"));
            }
        }catch (Exception e){
            logger.error(RecUtils.getErrorString("pack()"), e);
        }

        return poiEntryList;
    }

    /**
     * 增加ct_poi字样
     * @param poiEntryList
     * @param request
     * @param joLog
     * @return
     */
    public List<PoiEntry> addCtPoi(List<PoiEntry> poiEntryList, RecRequest request, JSONObject joLog){
        String fun = "hotel_rec_aggregation";
        String type = request.getServiceType().name();
        if (RecServiceType.POI_DETAIL_REC == request.getServiceType()){
            fun = "hotel_poidetail_rec";
        }

        String stid = "0";
        try {
            stid = recStidClient.register(fun, "strategy", request.getStrategy(), "type", type);
        } catch (Exception e){
            logger.warn(RecUtils.getWarnString("ct_poiRegister"), e);
        }

        int index = 0;
        long globalId = genGlobalId(request.getUuid(), System.currentTimeMillis());
        for (PoiEntry poiEntry: poiEntryList){
            poiEntry.setCt_poi(stid + "_c" + index + "_e" + globalId);
            poiEntry.setStid(stid);
            index += 1;
        }
        try {
            joLog.put("st_id", stid);
            joLog.put("global_id", globalId);
        }catch (Exception e){
            logger.warn("[WARN] jsonLogERROR", e);
        }

        return poiEntryList;
    }


    public long genGlobalId(String uuid, long timestamp){
        StringBuilder sb = new StringBuilder();
        sb.append(uuid);
        sb.append(timestamp);
        sb.append(RandomUtils.nextInt());
        return RecUtils.hashString2Long(String.valueOf(sb.toString()));
    }

}
