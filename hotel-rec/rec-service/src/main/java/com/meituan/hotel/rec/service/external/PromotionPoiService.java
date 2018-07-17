package com.meituan.hotel.rec.service.external;


import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import com.meituan.hotel.rec.service.constants.MedisClient;
import com.meituan.hotel.rec.service.utils.RecUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Map;

/**
 * PromotionPoiService.java
 * 获取运营poi及对应的权重，返回格式Map(poiId -> weight)
 * 调用方法：PromotionPoiService.getInstance().getPoiMap()
 * @author jiangweisen@meituan.com
 * @date 2016-03-25
 * @brief
 */

public class PromotionPoiService {
    private static volatile PromotionPoiService instance = null;
    private static volatile long lastUpdateTime = 0;

    private static Map<Integer, Double> poiMap = Maps.newHashMap();

    private static final long EXPIRE_TIME = 3600 * 1000;
    private static final Logger logger = RecUtils.getLogger(PromotionPoiService.class.getSimpleName());

    private PromotionPoiService() {
        updateData();
    }

    public static synchronized boolean isExpired() {
        long now = System.currentTimeMillis();
        return now - lastUpdateTime > EXPIRE_TIME;
    }

    public static PromotionPoiService getInstance() {
        if (instance == null || isExpired()) {
            synchronized (PromotionPoiService.class) {
                if (instance == null || isExpired()) {
                    lastUpdateTime = System.currentTimeMillis();
                    instance = new PromotionPoiService();
                }
            }
        }
        return instance;
    }

    private void updateData() {
        try {
            Map<Integer, Double> newPoiMap = Maps.newHashMap();
            String value = MedisClient.medisClient.getString(MedisClient.PROMOTION_POI_KEY);

            if (StringUtils.isNotBlank(value)) {
                JSONArray jsonArray = JSONArray.parseArray(value);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONArray item = jsonArray.getJSONArray(i);
                    newPoiMap.put(item.getInteger(0), item.getDouble(1));
                }
            }

            poiMap = newPoiMap;
            logger.info("updateData() success, poiMap=" + poiMap);

        } catch (Exception e) {
            logger.error("updateData() error:" + e);
        }
    }

    public Map<Integer, Double> getPoiMap() {
        return poiMap;
    }
}
