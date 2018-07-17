package com.meituan.hotel.rec.service.rerank.common;

import java.util.Map;

/**
 * Created by hehuihui on 3/25/16
 * rerank环节的请求
 */
public class RerankRequest {
    private Map<Integer,Map<String,Double>> poiIdsFeatureMap;
    private long userId = -1;

    public Map<Integer, Map<String, Double>> getPoiIdsFeatureMap() {
        return poiIdsFeatureMap;
    }

    public void setPoiIdsFeatureMap(Map<Integer, Map<String, Double>> poiIdsFeatureMap) {
        this.poiIdsFeatureMap = poiIdsFeatureMap;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
