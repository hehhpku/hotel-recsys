package com.meituan.hotel.rec.service.utils;

import org.slf4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: hehuihui@meituan.com Date: 3/14/16
 */
public class RecDateUtils {
    public static final Logger logger = RecUtils.getLogger(RecDateUtils.class.getSimpleName());


    public static final long TOW_DAY_SECOND = 2 * 24 * 3600;

    public static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    /**
     * 获取当前时间的字符串格式
     *
     * @param sdf 输出时间的格式，默认为null,格式为yyyy-MM-dd HH:mm:ss
     * @return 返回当前时间的字符串形式
     */
    public static String getCurrentTimeString(SimpleDateFormat sdf) {
        if (sdf == null)
            sdf = SIMPLE_DATE_TIME_FORMAT;
        return sdf.format(new Date());
    }

    /**
     * 解析时间yyyy-MM-dd HH:mm:ss成时间戳，精确度为s
     * @param s
     * @return
     */
    public static Long parseTimeString(String s) {
        Long timestamp = System.currentTimeMillis() / 1000;
        try {
            timestamp = SIMPLE_DATE_TIME_FORMAT.parse(s).getTime() / 1000;
        } catch (Exception e) {
            logger.error(RecUtils.getErrorString("parseTimeString"), e);
        }
        return timestamp;
    }

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
     * 获取日期，Int型返回
     * @param dayIndex
     * @return
     */
    public static int getDayNumFormat(int dayIndex){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, dayIndex);
        int day = Integer.parseInt( simpleDateFormat.format(new Date()) );
        return day;
    }

    /**
     * 获取当天是星期几
     * @return
     */
    public static int getDayOfWeek(){
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (day == 0){
            day = 7;
        }
        return day;
    }

    /**
     * 获取当前的小时
     * @return
     */
    public static int getHourOfDay(){
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY);
    }
}
