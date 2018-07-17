package com.meituan.hotel.rec.service.postrank;

import com.google.common.collect.Maps;
import com.meituan.hotel.daedalus.base.common.HotelPoiFields;
import com.meituan.hotel.daedalus.client.HotelPoiClient;
import com.meituan.hotel.daedalus.client.model.HotelModel;
import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.constants.Constants;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.jmonitor.JMonitor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * HighStarHotelRule.java
 *
 * @author jiangweisen@meituan.com
 * @date 2016-05-16
 * @brief
 */

@Service("high-star-hotel-rule")
public class HighStarHotelRule {
    @Autowired
    private HotelPoiClient hotelPoiClient;

    private static final Logger logger = LoggerFactory.getLogger(Constants.LOG_PREFIX
            + HighStarHotelRule.class.getSimpleName());

    public Map<Integer, Integer> getHotelStarMap(RecRequest request, List<PoiEntry> poiEntryList)
    {
        if (CollectionUtils.isEmpty(request.getPoiOnShowList())) {
            return null;
        }

        List<Integer> poiIds = new ArrayList<Integer>();
        poiIds.add(request.getPoiOnShowList().get(0));
        for (PoiEntry poiEntry : poiEntryList) {
            poiIds.add(poiEntry.getPoiId());
        }

        List<String> hotelPoiFields = HotelPoiFields.getHotelFields();

        Map<Integer, Integer> hotelStarMap = Maps.newHashMap();
        try {
            List<HotelModel> hotelModels = hotelPoiClient.listHotels(poiIds, hotelPoiFields);
            for (HotelModel hotelModel : hotelModels) {
                Map<String, String> extMap = hotelModel.getExtMap();
                int poiId = hotelModel.getPoiId();
                String hotelStar = MapUtils.getString(extMap, HotelPoiFields.hotelStar);
                hotelStarMap.put(poiId, RecUtils.parseHotelStar(poiId, hotelStar));
            }
        } catch (Exception e) {
            logger.warn(RecUtils.getWarnString("getHotelModelMap()"), e);
            JMonitor.add(JmonitorKey.DAEDALUS_EXP);
        }

        return hotelStarMap;
    }

    public List<PoiEntry> rerankRule(RecRequest request, List<PoiEntry> poiEntryList) {
        long start = System.currentTimeMillis();

        if (CollectionUtils.isEmpty(request.getPoiOnShowList())) {
            return poiEntryList;
        }
        if (CollectionUtils.isEmpty(poiEntryList)) {
            return poiEntryList;
        }

        // 查询各个酒店的星级
        Map<Integer, Integer> hotelStarMap = this.getHotelStarMap(request, poiEntryList);

        int reqPoiId = request.getPoiOnShowList().get(0);
        if (MapUtils.isEmpty(hotelStarMap) || !hotelStarMap.containsKey(reqPoiId)){
            return poiEntryList;
        }

        // 当前酒店的星级
        try {
            final int reqHotelStar = MapUtils.getIntValue(hotelStarMap, reqPoiId);

            // 如果是高星酒店
            if (reqHotelStar < Constants.DEFAULT_HOTEL_STAR) {
                for (PoiEntry poiEntry : poiEntryList) {
                    int hotelStar = MapUtils.getIntValue(hotelStarMap, poiEntry.getPoiId(),
                            Constants.LOW_HOTEL_STAR);
                    poiEntry.setHotelStar(hotelStar);
                }

                Collections.sort(poiEntryList, new Comparator<PoiEntry>() {
                    @Override
                    public int compare(PoiEntry o1, PoiEntry o2) {
                        int diffStar1 = Math.abs(o1.getHotelStar() - reqHotelStar);
                        int diffStar2 = Math.abs(o2.getHotelStar() - reqHotelStar);

                        if (diffStar1 == diffStar2) {
                            return Double.compare(o2.getRerankScore(), o1.getRerankScore());
                        } else {
                            return Integer.compare(diffStar1, diffStar2);
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error(RecUtils.getErrorString("HighStarHotelRule.rerankRule()"), e);
        }

        // 将同星级酒店排在前面（在filter步骤中去重）

        long timeCost = System.currentTimeMillis() - start;
        logger.info("[INFO] HighStarRule.rerankRule() cost " + timeCost + " ms");
        JMonitor.add(JmonitorKey.HIGH_STAR_RULE_TIME, timeCost);

        return poiEntryList;
    }

}
