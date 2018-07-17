package com.meituan.hotel.rec.service.rerank.feature;

import com.meituan.hotel.rec.service.rerank.common.FeatureType;
import com.meituan.hotel.rec.service.rerank.common.RerankRequest;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Author: hehuihui@meituan.com Date: 12/15/15
 */
@Service("feature-assemble-service")
public class FeatureAssembler {

    private Map<FeatureType, IFeatureLoader> featureLoaderMap;

    /**
     * 封装gbdt模型需要的featureList
     * @param request
     * @return key=poiId, value = feature_value_list
     */
    public Map<Integer,List<Double>> assemblePoiIdsFeatureMap(RerankRequest request, Map<String, Integer> featureNameIndexMap){
        Map<Integer, List<Double>> poiIdsFeatureMap = new HashMap<Integer, List<Double>>();

        //判断应该加载哪类特征
        Map<String, FeatureType> featureTypeMap = FeatureUtils.getPoiFeatureTypeMap();
        Set<FeatureType> featureTypeSet = new HashSet<FeatureType>();
        for (String feature: featureNameIndexMap.keySet()){
            if (featureTypeMap.containsKey(feature)){
                featureTypeSet.add(featureTypeMap.get(feature));
            }
        }

        //初始化特征值
        List<Integer> poiIdList = new ArrayList<Integer>(request.getPoiIdsFeatureMap().keySet());
        long userId = request.getUserId();
        for (int poiId: poiIdList){
            poiIdsFeatureMap.put(poiId, FeatureUtils.getListWithFefaultValue(Double.MIN_VALUE, featureNameIndexMap.size()));
        }

        //加载特征值
        IFeatureLoader featureLoader;
        for (FeatureType type: featureTypeSet){
            if (!featureLoaderMap.containsKey(type)){
                continue;
            }
            featureLoader = featureLoaderMap.get(type);
            Map<Integer, Map<String, Double>> map = featureLoader.loadFeature(poiIdList, userId);
            for (int poiId: poiIdsFeatureMap.keySet()) {
                List<Double> featureValueList = poiIdsFeatureMap.get(poiId);
                for (Map.Entry<String, Double> e : map.get(poiId).entrySet()) {
                    if (!featureNameIndexMap.containsKey(e.getKey()))
                        continue;
                    int index = featureNameIndexMap.get(e.getKey());
                    double value = e.getValue();
                    featureValueList.set(index, value);
                }
            }
        }

        //组装request传递来的特征
        for (int poiId: poiIdsFeatureMap.keySet()){
            List<Double> featureValueList = poiIdsFeatureMap.get(poiId);
            for (Map.Entry<String,Double> e: request.getPoiIdsFeatureMap().get(poiId).entrySet()){
                if (!featureNameIndexMap.containsKey(e.getKey()))
                    continue;
                int index = featureNameIndexMap.get(e.getKey());
                double value = e.getValue();
                featureValueList.set(index, value);
            }
        }
        return poiIdsFeatureMap;
    }

    public void setFeatureLoaderMap(Map<FeatureType, IFeatureLoader> featureLoaderMap) {
        this.featureLoaderMap = featureLoaderMap;
    }
}
