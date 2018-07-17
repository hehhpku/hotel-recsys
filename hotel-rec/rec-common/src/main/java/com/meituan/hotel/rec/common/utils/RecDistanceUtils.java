package com.meituan.hotel.rec.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: jiangweisen,jiangweisen@meituan.com Date: 1/14/16
 */
public class RecDistanceUtils {
    public static final int EARTH_RADIUS = 6373000;                        //地球半径, 单位米
    public static final int GEO_HASH_PRECISE = 3;

    /**
     * 计算两个经纬度坐标的弧面距离
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @return
     * 1. 将数据转化为弧度
     * 2. 计算球面距离
     */
    public static double calDistanceAccurate(double longitude1, double latitude1, double longitude2, double latitude2)
    {
        longitude1 = degreeToAcr(longitude1);
        longitude2 = degreeToAcr(longitude2);
        latitude1  = degreeToAcr(latitude1);
        latitude2  = degreeToAcr(latitude2);

        double distance   = 0.0;
        double dLongitude = longitude1 - longitude2;
        double dLatitude  = latitude1 - latitude2;

        double a = Math.pow((Math.sin(dLatitude / 2)), 2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.pow(Math.sin(dLongitude/2),2);
        double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        distance = c * EARTH_RADIUS;
        return distance;
    }

    /**
     * 度转弧度
     * @param degree
     * @return
     */
    public static double degreeToAcr(double degree) {
        return degree / 180.0 * Math.PI;
    }

    public static String hashGeoLocation(double lat, double lng, int precise, int baseNum){
        int base = (int)Math.pow(10,baseNum);
        int latTemp = (int)(lat*base);
        int lngTemp = (int)(lng*base);
        return Integer.toString(latTemp / precise) + Integer.toString(lngTemp / precise);
    }

    private static String hashGeoLocation(int lat, int lng, int precise){
        return Integer.toString(lat / precise) + Integer.toString(lng / precise);
    }

    public static List<String> getNear9Keys(double lat, double lng, int precise,int baseNum){
        int base = (int)Math.pow(10,baseNum);

        List<String> keyList = new ArrayList<String>();
        int baseLat = (int) (lat * base);
        int baseLng = (int) (lng * base);

        for (int x = baseLat - precise; x <= baseLat + precise; x += precise){
            for (int y = baseLng - precise; y <= baseLng + precise; y += precise){
                keyList.add(hashGeoLocation(x, y, precise));
            }
        }
        return keyList;
    }

    public static void main(String[] args) {
        int base = 3;
        System.out.println((int)Math.pow(10,base));
        System.out.println(getNear9Keys(23.625,120.923,3,2));
    }


}
