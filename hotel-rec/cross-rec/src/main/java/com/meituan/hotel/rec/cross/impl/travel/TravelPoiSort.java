package com.meituan.hotel.rec.cross.impl.travel;


import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 根据poi的属性得分对poi进行排序
 * Created by zuolin on 15/11/4.
 */
public class TravelPoiSort {

    private static final Logger logger = LoggerFactory.getLogger(TravelPoiSort.class);
    private String poiKey = "Travel_(poiid, star, distance, consume, price, corr, typeid)";
    private static final int LOG_RES_LENGTH = 30;          //返回日志poi的数目
    private static final int AVG_HIGH_PRICE = 900;         //价格的最大值
    private static final int AVG_LOW_PRICE = 25;           //价格的最低价
    private static final int MAX_EVALUATE_NUM = 1500;      //评价的最大数目
    private static final int MIN_DISTANCE = 5000;          //设定的最小距离
    private static final int MAX_DISTANCE = 30000;         //设定推荐的最大距离
    private static final int MAX_CONSUME = 10000;           //poi的最大销量
    private static final int MIN_CONSUME = 100;            //poi的最小销量

    /**
     * 根据poi的属性评分进行排序
     * @param poiList
     * @return
     */
    public List<TravelOrHotelPoiInfo> sortTravelPoi(JSONObject jsodLog, List<TravelOrHotelPoiInfo> poiList, int hasStar){
        if (CollectionUtils.isEmpty(poiList)){
            return poiList;
        }
        for(TravelOrHotelPoiInfo travelOrHotelPoiInfo : poiList){
            travelOrHotelPoiInfo.setPoiScoreSum(calcPoiScore(travelOrHotelPoiInfo, hasStar));
        }
        Collections.sort(poiList, Collections.reverseOrder(new TravelOrHotelPoiInfo()));
        String poiValue = "";
        for(int i = 0 ;i < Math.min(LOG_RES_LENGTH,poiList.size()) ; i++){
            TravelOrHotelPoiInfo poiInfo = poiList.get(i);
            poiValue += "{" + poiInfo.getPoiId() + "," + poiInfo.getPoiBaseAttribute().toString() + ","
                      + poiInfo.getTypeId()+ "}";
        }
        try{
            if (jsodLog.has(poiKey)){
                poiValue = jsodLog.getString(poiKey) + poiValue ;
            }
            jsodLog.put(poiKey, poiValue);
        }catch (Exception e){
            logger.error(AvailField.JSON_SAVE_EXCEPTION ,e);
        }
        return poiList;
    }

    /**
     * 计算旅游poi的综合得分
     * @param travelOrHotelPoiInfo
     * @return double
     */
    public double calcPoiScore(TravelOrHotelPoiInfo travelOrHotelPoiInfo, int hasStar){
        //属性权重定义
        double disWeight = 2;
        double consumeWeight = 1;
        double corrWeight = 2;
        double priceWeight = 0;
        double evaluateWeight = 0;
        double starWeight = 0;

        // 属性分数计算
        double distanceScore = calcDisScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getDistance());
        double salesScore = calcConsumeScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getConsume());
        double corrScore = calcCorrScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getHotelTravelCorrScore());
        double priceScore = calcPriceScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getLowPrice(), travelOrHotelPoiInfo.getPoiBaseAttribute().getHighPrice());
        double evaluateScore = calcEvaluateScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getEvaluateCnt(), travelOrHotelPoiInfo.getPoiBaseAttribute().getEvaluateScore());
        double starScore = calcStarScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getStar());

        //总分
        double sumScore = 0;
        sumScore = disWeight * distanceScore + consumeWeight * salesScore + corrWeight * corrScore + priceWeight * priceScore +
                evaluateWeight * evaluateScore + hasStar * starWeight * starScore;
        return sumScore;
    }

    /**
     * 旅游星级评分
     * @param star
     * @return
     */
    public double calcStarScore(String star){

        if("5A".equals(star)){
            return 2;
        }else if ("4A".equals(star)){
            return 1;
        }else if ("3A".equals(star)){
            return 0.8;
        }else if ("2A".equals(star)){
            return 0.5;
        }else if ("A".equals(star)) {
            return 0.5;
        }else {
            return 0;
        }
    }

    /**
     * 计算poi的价格得分
     * @param lowPrice
     * @param highPrice
     * @return
     */
    public double calcPriceScore(double lowPrice, double highPrice){
        double avgPrice = (lowPrice + highPrice) / 2;
        double priceScore = 0;
        try{
            if (avgPrice > AVG_HIGH_PRICE){
                priceScore = 0;
            }else if (avgPrice <= AVG_LOW_PRICE){
                priceScore = 1;
            }else{
                priceScore = (1 / (AVG_LOW_PRICE - AVG_HIGH_PRICE) )* (avgPrice - AVG_HIGH_PRICE);
            }
        }catch (Exception e){
            logger.error(AvailField.PRICE_SCORE_EXCEPTION,e);
        }
        return priceScore;
    }

    /**
     * 计算poi的评价得分
     * @param evaluateCnt
     * @param avgScore
     * @return
     */
    public double calcEvaluateScore(int evaluateCnt, double avgScore){
        double evaluateScore = 0.0;
        double evaluateSum = evaluateCnt * avgScore;
        try{
            if(evaluateSum > MAX_EVALUATE_NUM){
                evaluateSum = MAX_EVALUATE_NUM;
            }
            evaluateScore = evaluateSum / MAX_EVALUATE_NUM;
        }catch (Exception e){
            logger.error(AvailField.EVALUATE_SCORE_EXCEPTION,e);
        }
        return evaluateScore;
    }

    /**
     * 计算距离得分
     * @param distance
     * @return double
     */
    public double calcDisScore(double distance){
        if((distance - 0.0) < 0.0001){
            return 0;
        }
        double disScore = 0;
        try{
            if(distance < MIN_DISTANCE){
                disScore = 1;
            }else{
                disScore = 1 - distance / MAX_DISTANCE;
            }
        }catch(Exception e){
            logger.error(AvailField.DISTANCE_SCORE_EXCEPTION ,e);
        }
        return disScore;
    }

    /**
     * 计算销量的得分
     * @param consume
     * @return double
     */
    public double calcConsumeScore(double consume){
        if((consume - 0.0) < 0.0001){
            return 0;
        }
        double consumeScore = 0;
        try{
            if(consume > MAX_CONSUME){
                consumeScore = 1;
            }else if (consume < MIN_CONSUME){
                consumeScore = 0;
            }else {
                consumeScore = (consume - MIN_CONSUME) / (MAX_CONSUME - MIN_CONSUME);
            }
        }catch(Exception e){
            logger.error(AvailField.CONSUME_SCORE_EXCEPTION,e);
        }
        return consumeScore;
    }

    /**
     * 相关度得分
     * @param corrScore
     * @return
     */
    public double calcCorrScore(double corrScore){
        return corrScore;
    }

    /**
     * 保留数字到小数点后4位
     * @param score
     * @return
     */
    public double roundDouble(double score){
        return Math.round(score * 10000.0)/10000.0;
    }
}