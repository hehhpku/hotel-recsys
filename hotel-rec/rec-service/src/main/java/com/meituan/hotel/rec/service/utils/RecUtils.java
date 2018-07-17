package com.meituan.hotel.rec.service.utils;


import com.alibaba.fastjson.JSON;
import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.PoiSource;
import com.meituan.hotel.rec.service.constants.Constants;

import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Author: hehuihui@meituan.com Date: 3/15/16
 */
public class RecUtils {
    public static final  Logger logger = RecUtils.getLogger(RecUtils.class.getSimpleName());

    /**
     * 获取Resource目录下的配置文件
     * @param filename
     * @return String
     */
    public static String getResourceFile(String filename) {
        return RecUtils.class.getClassLoader().getResource(filename).getFile();
    }

    public static Logger getLogger(String className){
        return LoggerFactory.getLogger(Constants.LOG_PREFIX + className);
    }


    public static String getTimeCostString(String fun, long timeCost){
        return "[TIME] " + fun + " cost " + timeCost + " ms.";
    }

    public static String getWarnString(String fun){
        return "[WARN] " + fun;
    }

    public static String getErrorString(String msg){
        return "[ERROR] " + msg;
    }
    public static String getInfoString(String msg){
        return "[INFO] " + msg;
    }

    public static int comparePoiScore(PoiEntry e1, PoiEntry e2){
        if (e1.getSource() == PoiSource.NEAR_BY){
            return 1;
        } else if (e2.getSource() == PoiSource.NEAR_BY){
            return -1;
        } else {
            return 0;
        }
    }

    public static long hashString2Long(String value) {
        long h = 0;
        if (value.length() > 0) {

            for (int i = 0; i < value.length(); i++) {
                h = 31 * h + value.charAt(i);
            }
        }
        return Math.abs(h);
    }

    /**
     * 对列表去重，保留第一个出现的元素
     * @param list
     * @param <T>
     * @return
     */
    public static<T> List<T> convert2DistinctList(List<T> list){
        if (CollectionUtils.isEmpty(list)){
            return list;
        }

        Set<T> set = new HashSet<T>();
        List<T> listDistinct = new ArrayList<T>(list.size());
        for (T t: list){
            if (!set.contains(t)){
                listDistinct.add(t);
            }
            set.add(t);
        }
        return listDistinct;
    }

    /**
     * 将TBase类转化成json
     * @param t
     */
    public static void putToJson(TBase t, JSONObject json){
        try{
            TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
            String s = serializer.toString(t);
            json.put(t.getClass().getSimpleName(), JSON.parse(s));
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("putToJson()"),e);
        }
    }

    /**
     * 将TBase类转化成json
     * @param t
     */
    public static String getJSONString(TBase t){
        String result = "null";
        try{
            TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
            String s = serializer.toString(t);
            result = JSON.parse(s).toString();
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("putToJson()"),e);
        }
        return result;
    }

    public static<T,S> String entryListToString(List<Map.Entry<T,S>> entryList, String s){
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<T,S> e: entryList){
            stringBuilder.append(e.getKey().toString());
            stringBuilder.append(s);
            stringBuilder.append(e.getValue());
            stringBuilder.append(",");
        }
        if (stringBuilder.length() > 0)
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    public static int parseHotelStar(int poiId, String hotelStar) {
        int intHotelStar = Constants.LOW_HOTEL_STAR;
        try {
            intHotelStar = Integer.parseInt(hotelStar);
        } catch (Exception e) {
            logger.warn(RecUtils.getWarnString("parseHotelStar(): " + poiId) + "; " + e);
        }
        return intHotelStar;
    }
}
