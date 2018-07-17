package com.meituan.hotel.rec.service.rerank.feature.implement;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meituan.hotel.rec.service.constants.MedisClient;
import com.meituan.hotel.rec.service.rerank.common.FeatureType;
import com.meituan.hotel.rec.service.rerank.feature.FeatureUtils;
import com.meituan.hotel.rec.service.rerank.feature.IFeatureLoader;
import com.meituan.hotel.rec.service.utils.RecUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by hehuihui on 3/25/16
 */
@Service("user-dyf-feature-loader")
public class UserHistoryFeatureLoader implements IFeatureLoader {
    private static final Logger logger = RecUtils.getLogger(UserHistoryFeatureLoader.class.getSimpleName());
    private final static String USER_DYF_INDEX_FILE = RecUtils.getResourceFile("data/user_dynamic_feature_medisKey2gbdtKey");
    private static Map<String, String> userFeatureMedisKey2GBDTKeyMap = new HashMap<String, String>();


    static {
        userFeatureMedisKey2GBDTKeyMap = loadUserDynamicFeatureMedisKey2GBDTKeyMap(USER_DYF_INDEX_FILE);
        FeatureUtils.updateFeatureType(userFeatureMedisKey2GBDTKeyMap.values(), FeatureType.USER_DYNAMIC_FEATURE);
    }

    @Override
    public Map<Integer, Map<String, Double>> loadFeature(List<Integer> poiIdList, long userId) {
        Map<Integer,Map<String,Double>> map = new HashMap<Integer,Map<String,Double>>();
        if (CollectionUtils.isEmpty(poiIdList))
            return map;
        //初始化各个特征的取值
        for (int poiId: poiIdList){
            Map<String, Double> featuerValueMap = new HashMap<String, Double>();
            for (String feature: userFeatureMedisKey2GBDTKeyMap.values()){
                featuerValueMap.put(feature,0.0);
            }
            map.put(poiId, featuerValueMap);
        }
        //userId <= 0，意味着没有user维度的特征
        if (userId <= 0){
            return map;
        }
        //从medis获取user特征，并解析，存储到userFeatureMap，格式featureName -> (poiId -> times)
        String medisKey = MedisClient.MEDIS_USER_DYF_PREFIX + userId;
        String userDynamicFeatureString = MedisClient.medisClient.getString(medisKey);
        Map<String, Map<Integer,Integer>> userFeatureMap = new HashMap<String, Map<Integer, Integer>>();
        if (userDynamicFeatureString != null){
            JSONObject jo = JSON.parseObject(userDynamicFeatureString);
            for (String key: jo.keySet()){
                String[] poiIdTimesStringList = StringUtils.split(jo.getString(key), ",");
                Map<Integer, Integer> poiIdTimesMap = new HashMap<Integer, Integer>();
                //view的特征有次数
                if (key.startsWith("v")){
                    for (String poiIdString: poiIdTimesStringList) {
                        String[] poiIdTimeString = poiIdString.split("=");
                        int poiId = Integer.parseInt(poiIdTimeString[0]);
                        int times = Integer.parseInt(poiIdTimeString[1]);
                        poiIdTimesMap.put(poiId, times);
                    }
                }else { //其他特征只是列表，没有次数
                    for (String poiIdString: poiIdTimesStringList){
                        int poiId = Integer.parseInt(poiIdString);
                        poiIdTimesMap.put(poiId,1);
                    }
                }
                userFeatureMap.put(key,poiIdTimesMap);
            }
        }

        //更新user维度的特征
        for (int poiId: poiIdList){
            Map<String, Double> featureValueMap = map.get(poiId);
            for (String featureName: userFeatureMap.keySet()){
                Map<Integer, Integer> poiIdTimesMap = userFeatureMap.get(featureName);
                if (poiIdTimesMap.containsKey(poiId)){
                    featureValueMap.put(userFeatureMedisKey2GBDTKeyMap.get(featureName), poiIdTimesMap.get(poiId).doubleValue());
                }
            }
        }

        return map;
    }

    /**
     * 读取user 动态特征中,medisKey和gbdt Key的映射关系
     * 为了节约存储空间，medis的key使用的是简写的
     * @param file 文件地址
     * @return medisKey -> gbdtKey 的map
     */
    private static Map<String, String> loadUserDynamicFeatureMedisKey2GBDTKeyMap(String file){
        Map<String, String> map = new HashMap<String, String>();
        try {
            FileInputStream fis = new FileInputStream(file);
            Scanner scanner = new Scanner(fis);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] aString = line.split(":");
                if (aString.length != 2 || line.startsWith("#"))
                    continue;
                map.put(aString[1],aString[0]);
            }
            fis.close();
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("loadUserDynamicFeatureMedisKey2GBDTKeyMap()"), e);
        }
        logger.info(RecUtils.getInfoString("loadUserDynamicFeatureMedisKey2GBDTKeyMap"));
        return map;
    }
}
