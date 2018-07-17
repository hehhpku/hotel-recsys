package com.meituan.hotel.rec.service.rerank.common;

import com.meituan.hotel.rec.thrift.RecServiceStatus;

import java.util.Map;

/**
 * Created by hehuihui on 3/25/16
 */
public class RerankResponse {
    private RecServiceStatus serviceStatus;

    private Map<Integer,Double> poiidPredictScoreMap;

    public RecServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(RecServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public Map<Integer, Double> getPoiidPredictScoreMap() {
        return poiidPredictScoreMap;
    }

    public void setPoiidPredictScoreMap(Map<Integer, Double> poiidPredictScoreMap) {
        this.poiidPredictScoreMap = poiidPredictScoreMap;
    }
}
