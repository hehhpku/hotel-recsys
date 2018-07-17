package com.meituan.hotel.rec.service.recall.implement;

import com.alibaba.fastjson.JSONArray;
import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.constants.*;
import com.meituan.hotel.rec.service.recall.IRecaller;
import com.meituan.hotel.rec.service.recall.common.RecallRequest;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.service.utils.SearchUtil;
import com.meituan.hotel.rec.service.utils.TransformationUtils;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.util.CollectionUtil;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by jiangweisen, hehuihui on 3/16/16
 * 利用品牌名称获取某城市下该品牌的酒店
 *
 */
@Service("brand-poi-recall-service")
public class BrandPoiRecaller implements IRecaller{
    private static final Logger logger = RecUtils.getLogger( BrandPoiRecaller.class.getSimpleName());

    public static final String STRATEGY = "brandPoiRecallStrategy";

    @Override
    public RecResponse recall(RecallRequest request, JSONObject joLog) {
        long start = System.currentTimeMillis();
        String query = request.getQuery();
        String cityName = request.getChannelCityName();
        List<Term> segList = ToAnalysis.parse(query);
        List<String> keys = buildBrandKey(cityName, segList);
        List<PoiEntry> poiEntryList = getBrandPoiFromMedis(keys);
        for (PoiEntry e: poiEntryList){
            double distance = RecDistanceUtils.calDistance(e.getLocation(), request.getRequestLocation());
            e.setDistanceToRequest(distance);
        }

        for (PoiEntry e: poiEntryList){
            e.setSource(PoiSource.SIM_BRAND);
        }
        poiEntryList = CollectionUtil.subList(poiEntryList, 0, 300);

        RecResponse response = new RecResponse();
        response.setPoiEntryList(poiEntryList);
        response.setServiceStatus(RecServiceStatus.OK);
        response.setStrategy(STRATEGY);

        try{
            joLog.put("similarPoiList", TransformationUtils.getPoiIdListFromPoiEntry(poiEntryList));
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("recall()"), e);
        }

        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.BRAND_RECALL_TIME, timeCost);
        return response;
    }

    /**
     * 利用cityName和分词结果组装medis的key
     * @param city
     * @param segList
     * @return
     */
    public static List<String> buildBrandKey(String city, List<Term> segList) {
        // 将分词结果，组合成key
        List<String> keyList = new ArrayList<String>();
        for (int i = 0; i < segList.size(); i++) {
            Term term = segList.get(i);
            String name = term.getName();
            name = name.replace("市", "");

            // 去除停用词、城市名
            if (SearchUtil.getStopwordMap().containsKey(name) || name.equals(city)) {
                continue;
            }

            // 单词
            if (name.getBytes().length > 3 || name.length() > 1) {
                String key = city + "_" + name;
                keyList.add(key);

                // 同义品牌改写
                if (SearchUtil.getSynonymMap().containsKey(name)) {
                    key = city + "_" + SearchUtil.getSynonymMap().get(name);
                    keyList.add(key);
                }
            }

            // bigram 组合词
            if (i + 1 < segList.size()) {
                String bigram = name + segList.get(i + 1).getName();
                if (!SearchUtil.getStopwordMap().containsKey(bigram)) {
                    String key = city + "_" + bigram;
                    keyList.add(key);
                }
            }
        } // end for

        return keyList;
    }

    /**
     * 从medis获取品牌poi的数据
     * @param keyList
     * @return
     */
    public static List<PoiEntry> getBrandPoiFromMedis(List<String> keyList) {
        // 查询medis，找出相应品牌的poi
        Map<String, String> result = MedisClient.medisClient.multiGetString(keyList);

        // 归并
        List<PoiEntry> poiInfoList = new ArrayList<PoiEntry>();
        Set<Integer> poiIdSet = new HashSet<Integer>();
        for (Map.Entry<String, String> entry : result.entrySet()) {
            if (entry.getValue() != null) {
                List<PoiEntry> tempList = parsePoiBasicInfo(entry.getValue());

                // 去重
                for (PoiEntry tempInfo : tempList) {
                    int poiId = tempInfo.getPoiId();
                    if (poiIdSet.contains(poiId)) {
                        continue;
                    }
                    poiInfoList.add(tempInfo);
                    poiIdSet.add(poiId);
                }
            }
        }

        return poiInfoList;
    }

    /**
     * 解析medis品牌酒店的数据
     * @param json
     * @return
     */
    public static List<PoiEntry> parsePoiBasicInfo(String json) {
        List<PoiEntry> poiInfoList = new ArrayList<PoiEntry>();
        if (json == null) {
            return poiInfoList;
        }
        try {
            JSONArray ja = JSONArray.parseArray(json);
            for (int i = 0; i < ja.size(); i++) {
                JSONArray item = ja.getJSONArray(i);
                PoiEntry poiInfo = new PoiEntry(item.getInteger(0));
                double lat = item.getDouble(1);
                double lng = item.getDouble(2);
                poiInfo.setLocation(new Location(lat, lng));

                poiInfoList.add(poiInfo);
            }
        } catch (Exception e) {
            logger.error("parsePoiBasicInfo() error:" + e);
        }

        return poiInfoList;
    }
}
