package com.meituan.hotel.rec.service.utils;

import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.thrift.PoiRecInfo;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hehuihui on 3/21/16
 */
public class TransformationUtils {
    public static final Logger logger = RecUtils.getLogger(TransformationUtils.class.getSimpleName());


    public static List<Integer> getPoiIdListFromPoiRecInfo(List<PoiRecInfo> poiEntryList){
        List<Integer> poiIdList = new ArrayList<Integer>();
        if (CollectionUtils.isEmpty(poiEntryList)){
            return poiIdList;
        }
        for (PoiRecInfo entry: poiEntryList){
            poiIdList.add(entry.getPoiId());
        }
        return poiIdList;
    }

    public static List<Integer> getPoiIdListFromPoiEntry(List<PoiEntry> poiEntryList){
        List<Integer> poiIdList = new ArrayList<Integer>();
        if (CollectionUtils.isEmpty(poiEntryList)){
            return poiIdList;
        }
        for (PoiEntry entry: poiEntryList){
            poiIdList.add(entry.getPoiId());
        }
        return poiIdList;
    }

    /**
     * 获取Resource目录下的配置文件
     * @param filename
     * @return String
     */
    public static String getResourceFile(String filename) {
        String filePath = TransformationUtils.class.getClassLoader().getResource(filename).getFile();
        return filePath;
    }

    public static<T> List<T> parseIntegerString(String s, String sep){
        List<T> integerList = new ArrayList<T>();
        if (s == null || sep == null)
            return integerList;

        String[] infoList = s.split(sep);
        for (String info: infoList){
            try{
                integerList.add((T)info);
            } catch (Exception e){
                logger.error(RecUtils.getErrorString("parseIntegerString()"), e);
            }
        }

        return integerList;
    }
}
