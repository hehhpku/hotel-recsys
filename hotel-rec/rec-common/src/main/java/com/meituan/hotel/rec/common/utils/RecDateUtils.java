package com.meituan.hotel.rec.common.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Author: jiangweisen,jiangweisen@meituan.com Date: 1/14/16
 */
public class RecDateUtils {

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
            System.out.println("parse error");
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
        int day = Integer.parseInt( simpleDateFormat.format(calendar.getTime()) );
        return day;
    }

    public static int getDayNumFormat(int datIndex, int today){
        List<Integer> timeList = getDateTime(today);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(timeList.get(0),timeList.get(1)-1,timeList.get(2));
        calendar.add(Calendar.DATE, datIndex);
        return Integer.parseInt(simpleDateFormat.format(calendar.getTime()));
    }

    public static List<Integer> getDateTime(int today){
        int year = today / 10000;
        int month = (today - year*10000) / 100;
        int date = today - year*10000 - month*100;
        return Arrays.asList(year, month, date);
    }

    public static void main(String[] args) {
        System.out.println(getDayNumFormat(-5,20150505));
    }

}
