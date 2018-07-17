package com.meituan.hotel.rec.service.rerank.feature;

import com.meituan.hotel.rec.service.rerank.common.FeatureType;
import com.meituan.hotel.rec.service.utils.*;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.util.*;

/**
 * Author: hehuihui@meituan.com Date: 12/15/15
 */
public class FeatureUtils {

    private static Map<String, FeatureType> poiFeatureTypeMap = new HashMap<String, FeatureType>();

    private static final Logger logger = RecUtils.getLogger(FeatureUtils.class.getSimpleName());

    static {
        // 配置特征属性
        poiFeatureTypeMap.put(FeatureName.VIEW_PAY_PROBABILITY, FeatureType.USER_REAL_TIME_FEATURE);
    }

    public static Map<String, FeatureType> getPoiFeatureTypeMap() {
        return poiFeatureTypeMap;
    }

    /**
     * 读取feature->index的映射，构造map(key=featureName, value=index)
     * @param file featureIndex 文件
     * @return map(key=featureName, value=index)
     */
    public static Map<String,Integer> loadFeatureIndex(String file){
        Map<String,Integer> map = new HashMap<String, Integer>();
        try {
            FileInputStream fis = new FileInputStream(file);
            Scanner scanner = new Scanner(fis);
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] aString = line.split(":");
                if (aString.length != 2 || line.startsWith("#"))
                    continue;
                map.put(aString[0],Integer.parseInt(aString[1]));
            }
            fis.close();
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("loadFeatureIndex()"), e);
        }
        return map;
    }

    public static void updateFeatureType(Collection<String> featureList, FeatureType type){
        if (poiFeatureTypeMap == null){
            poiFeatureTypeMap = new HashMap<String, FeatureType>();
        }
        if (CollectionUtils.isEmpty(featureList)){
            return;
        }

        for (String s: featureList){
            poiFeatureTypeMap.put(s, type);
        }
    }

    /**
     * 初始化各个feature的值
     * @param featureList
     * @param initialValue
     * @return
     */
    public static Map<String, Double> getEmptyFeatureMapFromList(List<String> featureList, Double initialValue){
        Map<String, Double> map = new HashMap<String, Double>();
        if (CollectionUtils.isEmpty(featureList))
            return map;

        for (String feature : featureList){
            map.put(feature,initialValue);
        }
        return map;
    }

    /**
     * 取一个size长的默认值为t的列表
     * @param t
     * @param size
     * @param <T>
     * @return
     */
    public static<T> List<T> getListWithFefaultValue(T t, int size){
        if (size <= 0){
            return new ArrayList<T>();
        }
        List<T> list = new ArrayList<T>(size);
        for (int index = 0; index < size; index++){
            list.add(t);
        }
        return list;
    }


}
