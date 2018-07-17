package com.meituan.hotel.rec.common.utils;

import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Author: jiangweisen,jiangweisen@meituan.com Date: 1/31/16
 */
public class QPSDynamicController {

    private int maxQPS;
    private int maxReserveTime;
    private Map<String, List<Long>> keyTimestampsMap;
    private Map<String, Integer> QPSLimitMap;

    public double getHistoryQPS(String appKey){
        int qps = 0;
        if (!keyTimestampsMap.containsKey(appKey)){
            throw new IllegalArgumentException(appKey + " is not found, please add it before check!");
        }
        List<Long> timeseries = keyTimestampsMap.get(appKey);
        if (!CollectionUtils.isEmpty(timeseries))
            qps = (int)(timeseries.get(timeseries.size() - 1) - timeseries.get(0) ) / timeseries.size();
        return qps;
    }

    public void addAppKey(String appKey){
        long currentTimestamp = System.currentTimeMillis();
        keyTimestampsMap.put(appKey, new LinkedList<Long>());
        keyTimestampsMap.get(appKey).add(currentTimestamp);
        QPSLimitMap.put(appKey,maxQPS);
    }

    private void updateTimestamp(String key, long timestamp){
        List<Long> l = keyTimestampsMap.get(key);
        if (l.size() > maxReserveTime){
            l.remove(0);
        }
        l.add(timestamp);
    }

    public boolean checkValid(String appKey) throws IllegalArgumentException {
        long currentTimestamp = System.currentTimeMillis();

        if (!keyTimestampsMap.containsKey(appKey))
            throw new IllegalArgumentException(appKey + " is not found, please add it before check!");

        List<Long> timeSeries = keyTimestampsMap.get(appKey);
        if (CollectionUtils.isEmpty(timeSeries)){
            updateTimestamp(appKey, currentTimestamp);
            return true;
        } else {
            long oldestTime = timeSeries.get(0);
            int qps = (int)(currentTimestamp - oldestTime) / timeSeries.size();
            if (qps < QPSLimitMap.get(appKey)){
                updateTimestamp(appKey, currentTimestamp);
                return true;
            } else {
                return false;
            }

        }
    }

}
