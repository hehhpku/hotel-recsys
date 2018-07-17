package com.meituan.hotel.rec.service.rawRank;

import com.meituan.hotel.rec.service.common.PoiSource;
import com.meituan.hotel.rec.service.utils.RecUtils;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.util.*;

/**
 * Created by hehuihui on 3/22/16
 */
public class ScoreCalculator {

    private static final Logger logger = RecUtils.getLogger(ScoreCalculator.class.getSimpleName());

    private static Map<Integer, Integer> brandSimMap;

    private static HashMap<Integer, String> hotelTypeMap;

    static {
        brandSimMap = readBrandSim();

        hotelTypeMap = new HashMap<Integer, String>();
        hotelTypeMap.put(20, "BX");  //不限
        hotelTypeMap.put(79, "JL");  //经济连锁
        hotelTypeMap.put(80, "SW");  //商务
        hotelTypeMap.put(381, "ZT"); //主题
        hotelTypeMap.put(382, "DJ"); //度假
        hotelTypeMap.put(383, "GY"); //公寓
        hotelTypeMap.put(384, "KZ"); //客栈
        hotelTypeMap.put(385, "QN"); //青年
    }

    /**
     * 计算品牌的得分（原始得分公式，相似品牌得0.3分，命中品牌得1分）
     * @param t_brandId poi的品牌
     * @param brandIdList 品牌要求列表
     * @return 返回品牌的得分
     */
    public static double calBrandScore(int t_brandId, List<Integer> brandIdList){
        double brandIdScore = 0;

        if (CollectionUtils.isEmpty(brandIdList))
            return brandIdScore;

        for (int brandIdTemp : brandIdList) {
            if (t_brandId == brandIdTemp){    //酒店品牌相同, 得1分, 酒店品牌相似, 得0.3分
                brandIdScore = 1;
                break;
            } else if(brandSimMap.containsKey(t_brandId) && brandSimMap.containsKey(brandIdTemp)){
                if (brandSimMap.get(t_brandId) == brandSimMap.get(brandIdTemp))
                    brandIdScore = 0.3;
            }
        }
        return brandIdScore;
    }

    /**
     * 计算酒店类型评分
     * @param hotelTypeList
     * @param hotelTypeRequestList
     * @return
     */
    public static double calHotelTypeScore(String[] hotelTypeList, List<Integer> hotelTypeRequestList){
        double score = 0;
        if (CollectionUtils.isEmpty(hotelTypeRequestList))
            return score;
        //若没有住宿类型要求，且该酒店是青旅，降低分数
        if (hotelTypeRequestList.contains(20)) {
            if (Arrays.asList(hotelTypeList).contains("QN"))
                score = -0.5;
            else
                score = 0;
        }  else {
            for (int hotelType: hotelTypeRequestList){
                String hotelRequestS = hotelTypeMap.get(hotelType);
                if (Arrays.asList(hotelTypeList).contains(hotelRequestS))
                    score = 1;
            }
        }
        return score;
    }

    /**
     * 计算距离得分
     * @param distanceToRequest
     * @param maxDistance 筛选距离
     * @return
     */
    public static double calDistanceScore(double distanceToRequest, double maxDistance){
        if (distanceToRequest > 0.001 && maxDistance > 0.001) {
            double tempLocationScore = 1 - Math.sqrt(distanceToRequest / maxDistance);
            return Math.max(0, tempLocationScore);
        } else {
            return 1;
        }
    }

    public static double calDistanceScore2(double distance, double maxDistance){
        if (distance <= 0.001 || maxDistance <= 0.001) {
            return 0.0;
        }
        if (distance < 1000){
            return 1;
        }
        return (maxDistance - distance) / (maxDistance - 1000);
    }

    /**
     * 计算距离得分
     * @param distance
     * @param maxDistance 筛选距离
     * @return
     */
    public static double calDistanceScore(double distance, double maxDistance, PoiSource source, double saleScore){
        if (PoiSource.SIM_BRAND == source) {
            distance /= 2;
        }
        distance = Math.min(distance, maxDistance);

        double distanceScore;
        {
            if (Double.compare(distance,0) == 0) {
                distance = 1;
            }
            double ratio = maxDistance / distance;
            if (ratio >= 50) {
                distanceScore = Math.sqrt(50 + 1 / ratio);
            } else if (ratio < 2) {
                distanceScore = ratio / 5;
            } else {
                distanceScore = Math.sqrt(ratio);
            }
        }
        if (saleScore < 1) {
            distanceScore /= (2 - saleScore);
        }
        return distanceScore;
    }

    /**
     * 计算价格评分，返回评分结果
     * @param lowPrice 最低价
     * @param highPrice 最高价
     * @param price 价格
     * @return
     */
    public static double calPriceScore(double lowPrice, double highPrice, double price){
        double score = 0;
        if ( price > highPrice )
            score = Math.pow(highPrice / price,1.5);
        else if (price < lowPrice)
            score = Math.pow(price / lowPrice,1);
        else
            score = 1;

        return score;
    }

    /**
     * 计算价格评分
     * @param basePrice 基准价格
     * @param recPoiPrice 待推荐poi价格
     * @return
     */
    public static double calPriceScore(double basePrice, double recPoiPrice) {
        if (basePrice <= 0 || recPoiPrice <= 0) {
            return 0;
        }

        double minPrice = basePrice * 0.7;
        double maxPrice = basePrice * 1.2;

        if (recPoiPrice < minPrice) {
            return recPoiPrice / basePrice;
        }
        else if (recPoiPrice > maxPrice) {
            return basePrice / recPoiPrice;
        }
        else {
            return 1;
        }
    }

    /**
     * 计算销量和评价分的得分
     * @param grade 用户评价分
     * @param saleNum 销量
     * @return
     */
    public static double calSaleNumAndGradeScore(double grade, int saleNum){
        double maxGrade = 5;
        if (saleNum > 100)
            return grade /maxGrade;
        else{
            return (grade / maxGrade ) * (1 - Math.exp(-(double) saleNum /10) );
        }
    }

    /**
     * 销量得分
     * @param saleSum
     * @return
     */
    public static double calcSaleScore(int saleSum) {
        if (saleSum > 10000) {
            saleSum = 10000;
        }
        if (saleSum < 1) {
            return 0;
        }
        return Math.log10(saleSum);
    }

    /**
     * 计算评论得分
     * @param markScore poi评论分
     * @param markNum poi评论人数
     * @return
     */
    public static double calMarkScore(double markScore, int markNum) {
        double markNumScore = calcSaleScore(markNum);
        markScore = Math.max(2.0, markScore);
        markScore = Math.min(5.0, markScore);

        return Math.round((markScore - 4.0) / 0.25) * markNumScore;
    }

    /**
     * 读取酒店品牌相似性
     */
    public static Map<Integer,Integer> readBrandSim(){
        String path = RecUtils.getResourceFile("data/brandid.map" );
        Map<Integer,Integer> brandIdMap = new HashMap<Integer, Integer>();
        try{



            FileInputStream fis = new FileInputStream(path);
            Scanner scanner = new Scanner(fis);
            String[] brands = null;
            int rowNum = 1;
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                brands = line.split("\\s+");
                for (String id:brands){
                    brandIdMap.put(Integer.parseInt(id), rowNum);
                }
                rowNum += 1;
            }
            fis.close();
        }catch (Exception e){
            logger.error(RecUtils.getErrorString("readBrandSim()"), e);
        }
        return brandIdMap;
    }
}
