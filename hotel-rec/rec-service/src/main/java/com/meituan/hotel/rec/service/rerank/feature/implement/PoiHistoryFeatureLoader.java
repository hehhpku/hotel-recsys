package com.meituan.hotel.rec.service.rerank.feature.implement;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.meituan.hotel.rec.service.constants.MedisClient;
import com.meituan.hotel.rec.service.rerank.common.FeatureType;
import com.meituan.hotel.rec.service.rerank.feature.FeatureUtils;
import com.meituan.hotel.rec.service.rerank.feature.IFeatureLoader;
import com.meituan.hotel.rec.service.utils.RecUtils;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by hehuihui on 3/25/16
 */
@Service("poi-dyf-feature-loader")
public class PoiHistoryFeatureLoader implements IFeatureLoader {

    private static final Logger logger = RecUtils.getLogger(PoiHistoryFeatureLoader.class.getSimpleName());
    private static Map<Integer, String> poiDynamicFeatureNameIndexMap;
    private final static String POI_DYF_INDEX_FILE = RecUtils.getResourceFile("data/poi_dynamic_medis_index_map");

    static {
        poiDynamicFeatureNameIndexMap = loadIndex2poiDynamicFeatureNameMap(POI_DYF_INDEX_FILE);
        FeatureUtils.updateFeatureType(poiDynamicFeatureNameIndexMap.values(), FeatureType.POI_DYNAMIC_FEATURE);
    }

    @Override
    public Map<Integer, Map<String, Double>> loadFeature(List<Integer> poiIdList, long userId) {
        Map<Integer,Map<String,Double>> map = new HashMap<Integer,Map<String,Double>>();
        if (CollectionUtils.isEmpty(poiIdList))
            return map;

        List<String> keys = new ArrayList<String>();
        for (int poiId: poiIdList)
            keys.add(MedisClient.MEDIS_HOTEL_POI_DYF_PREFIX + poiId);
        Map<String,String> poiFeatureValueMap = MedisClient.medisClient.multiGetString(keys);

        int NUM_POI_DYF = poiDynamicFeatureNameIndexMap.size();
        for (int poiId: poiIdList){
            String dyf = poiFeatureValueMap.get(MedisClient.MEDIS_HOTEL_POI_DYF_PREFIX + poiId);
            Map<String,Double> featureValueMap = new HashMap<String, Double>();
            if (dyf != null){
                JSONArray ja = JSON.parseArray(dyf);
                int featureSize = Math.min(NUM_POI_DYF,ja.size());
                for (int index = 0; index < featureSize; index++) {
                    if (poiDynamicFeatureNameIndexMap.containsKey(index))
                        featureValueMap.put(poiDynamicFeatureNameIndexMap.get(index), ja.getDouble(index));
                }
                map.put(poiId,featureValueMap);
            } else {
                map.put(poiId, new HashMap<String,Double>());
            }
        }
        return map;
    }

    /**
     * 读取index->featurename的映射
     * @param file featureIndex 文件
     * @return map(key=index, value=featureName)
     */
    public static Map<Integer,String> loadIndex2poiDynamicFeatureNameMap(String file){
        Map<Integer,String> map = new HashMap<Integer,String>();
        try {
            FileInputStream fis = new FileInputStream(file);
            Scanner scanner = new Scanner(fis);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] aString = line.split(":");
                if (aString.length != 2 || line.startsWith("#"))
                    continue;
                map.put(Integer.parseInt(aString[1]),aString[0]);
            }
            fis.close();
        } catch (Exception e){
            logger.error("[ERROR] loadFeatureIndex()", e);
        }
        logger.info(RecUtils.getInfoString("loadIndex2poiDynamicFeatureNameMap()"));
        return map;
    }
}
