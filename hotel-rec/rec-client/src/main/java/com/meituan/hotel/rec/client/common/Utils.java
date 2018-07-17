package com.meituan.hotel.rec.client.common;

import com.meituan.hotel.rec.thrift.PoiRecInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by jiangweisen on 3/30/16
 */
public class Utils {
    public static final String SEPERATOR_COMMA = ",";

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    /**
     * 获取某天的字符串形式
     *
     * @param dayIndex         多少天之前，0为今天，dayIndex>0为dayIndex天后，dayIndex<0为abs(dayIndex)天前
     * @param simpleDateFormat 输出日期的格式，默认为null, 格式为yyyyMMdd
     * @return 返回日期的字符串形式
     */
    public static String getDayString(int dayIndex, SimpleDateFormat simpleDateFormat) {
        if (simpleDateFormat == null)
            simpleDateFormat = SIMPLE_DATE_FORMAT;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, dayIndex);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * 获取某天的整形形式
     *
     * @param dayIndex         多少天之前，0为今天，dayIndex>0为dayIndex天后，dayIndex<0为abs(dayIndex)天前
     * @param simpleDateFormat 输出日期的格式，默认为null, 格式为yyyyMMdd
     * @return 返回日期的字符串形式
     */
    public static int getDayNum(int dayIndex, SimpleDateFormat simpleDateFormat) {
        if (simpleDateFormat == null)
            simpleDateFormat = SIMPLE_DATE_FORMAT;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, dayIndex);
        return Integer.parseInt(simpleDateFormat.format(calendar.getTime()));
    }

    public static List<Integer> getIntListFromString(String s, String sep){
        List<Integer> list = new ArrayList<Integer>();

        try {
            if (StringUtils.isNotBlank(s)) {
                String[] infoList = s.split(sep);
                for (String temp: infoList){
                    list.add(Integer.parseInt(temp));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * 提取poiId列表
     * @param poiRecInfoList
     * @return
     */
    public static List<Integer> getPoiIdList(List<PoiRecInfo> poiRecInfoList){
        List<Integer> poiIdList = new ArrayList<Integer>();

        if (CollectionUtils.isNotEmpty(poiRecInfoList)) {
            for (PoiRecInfo info : poiRecInfoList) {
                poiIdList.add(info.getPoiId());
            }
        }

        return poiIdList;
    }
}
