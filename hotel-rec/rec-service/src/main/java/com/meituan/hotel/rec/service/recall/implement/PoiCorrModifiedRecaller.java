package com.meituan.hotel.rec.service.recall.implement;

import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.RecResponse;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.recall.IRecaller;
import com.meituan.hotel.rec.service.recall.common.RecallRequest;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.jmonitor.JMonitor;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

/**
 * Created by zuolin02 and hehuihui on 3/15/16
 */
@Service("poi-corr-v2p-llr-modified-recaller")
public class PoiCorrModifiedRecaller implements IRecaller {
    public static final String STRATEGY = "poiCorrModifiedRecallerStrategy";

    @Resource
    private PoiCorrV2PLLRRecaller poiCorrV2PLLRRecaller;

    private static final double CORR_THRESHOLD = 0.1;
    private static final double DISTANCE_THRESHOLD = 3000;

    @Override
    public RecResponse recall(RecallRequest request, JSONObject joLog) {
        long start = System.currentTimeMillis();

        RecResponse response = RecResponse.getErrorResponse();
        if (CollectionUtils.isEmpty(request.getPoiOnShow())){
            response.setStrategy(STRATEGY);
            response.setServiceStatus(RecServiceStatus.ERROR);
            return response;
        }

        response = poiCorrV2PLLRRecaller.recall(request, joLog);
        List<PoiEntry> poiEntryList = new ArrayList<PoiEntry>();
        if (response.getServiceStatus() == RecServiceStatus.OK) {
            poiEntryList = response.getPoiEntryList();
        }
        //去除相关度太小的poi
        if (CollectionUtils.isNotEmpty(poiEntryList)){
            List<PoiEntry> poiEntryListNew = new ArrayList<PoiEntry>();
            for (PoiEntry e: poiEntryList){
                if (e.getCorrScore() > CORR_THRESHOLD || e.getDistanceToRequest() < DISTANCE_THRESHOLD){
                    poiEntryListNew.add(e);
                }
            }
            poiEntryList = poiEntryListNew;
        }

        //召回结果太少时，返回状态是degrade
        if (poiEntryList.size() > 5) {
            response.setServiceStatus(RecServiceStatus.OK);
        } else {
            response.setServiceStatus(RecServiceStatus.DEGRADE);
        }
        response.setPoiEntryList(poiEntryList);
        response.setStrategy(STRATEGY);

        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.CITY_POI_RECALL_CORR_MOD_TIME, timeCost);

        return response;
    }
}
