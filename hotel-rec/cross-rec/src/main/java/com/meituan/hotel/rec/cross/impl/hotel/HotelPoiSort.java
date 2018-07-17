package com.meituan.hotel.rec.cross.impl.hotel;

import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 根据酒店poi的属性综合分对poi进行排序
 * Created by zuolin on 15/11/26.
 */
public class HotelPoiSort {
    private static final Logger logger = LoggerFactory.getLogger(HotelPoiSort.class);
    private String poiKey = "Hotel_(poiid,star,distance,consume,price,evaScore,sumScore)";
    private static final int LOG_RES_LENGTH = 20;             //返回日志poi的数目
    private static final double AVG_HIGH_PRICE = 700;         //价格的最大值
    private static final double AVG_LOW_PRICE = 100;           //价格的最低价
    private static final double MIN_DISTANCE = 1000;          //设定推荐的最小距离
    private static final double MAX_DISTANCE = 10000;         //设定推荐的最大距离
    private static final int MAX_CONSUME = 960;               //poi的最大销量
    private static final double MIN_EVASCORE = 0;             //最小评分
    private static final double MAX_EVASCORE = 5;             //最大评分

    /**
     * 根据酒店poi的评分对推荐列表进行排序
     * @param jsodLog
     * @param poiList
     * @return
     */
    public List<TravelOrHotelPoiInfo> sortHotelPoi(JSONObject jsodLog, List<TravelOrHotelPoiInfo> poiList){
        if (CollectionUtils.isEmpty(poiList)){
            return poiList;
        }
        for (TravelOrHotelPoiInfo travelOrHotelPoiInfo : poiList){
            travelOrHotelPoiInfo.setPoiScoreSum(calcPoiScore(travelOrHotelPoiInfo));
        }
        Collections.sort(poiList, Collections.reverseOrder(new TravelOrHotelPoiInfo()));
        String poiValue = "";
        for(int i = 0 ;i < Math.min(LOG_RES_LENGTH,poiList.size()) ; i++){
            TravelOrHotelPoiInfo poiInfo = poiList.get(i);
            poiValue += "{" + poiInfo.getPoiId() + "," + poiInfo.getPoiBaseAttribute().toString() +
                    "," + roundDouble(poiInfo.getPoiScoreSum()) + "}";
        }
        try{
            jsodLog.put(poiKey, poiValue);
        }catch (Exception e){
            logger.error(AvailField.JSON_SAVE_EXCEPTION,e);
        }
        return poiList;
    }

    /**
     * 计算酒店poi的综合得分
     * @param travelOrHotelPoiInfo
     * @return double
     */
    public double calcPoiScore(TravelOrHotelPoiInfo travelOrHotelPoiInfo){
        //属性权重定义
        double disWeight = 2;
        double consumeWeight = 1;
        double priceWeight = 1;
        double evaluateWeight = 0;

        // 属性分数计算
        double distanceScore = calcDisScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getDistance());
        double salesScore = calcConsumeScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getConsume());
        double priceScore = calcPriceScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getLowPrice(), travelOrHotelPoiInfo.getPoiBaseAttribute().getHighPrice());
        double evaluateScore = calcEvaluateScore(travelOrHotelPoiInfo.getPoiBaseAttribute().getEvaluateScore());

        //总分
        double sumScore = 0;
        sumScore = disWeight * distanceScore + consumeWeight * salesScore + priceWeight * priceScore +
                evaluateWeight * evaluateScore;
        return sumScore;
    }

    /**
     * 计算酒店的距离分
     * @param distance
     * @return
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
                disScore = (distance - MAX_DISTANCE) / (MIN_DISTANCE - MAX_DISTANCE);
            }
        }catch(Exception e){
            logger.error(AvailField.DISTANCE_SCORE_EXCEPTION,e);
        }
        return disScore;
    }

    /**
     * 计算酒店的销量分
     * @param consume
     * @return
     */
    public double calcConsumeScore(double consume){
        if((consume - 0.0) < 0.0001){
            return 0;
        }
        double consumeScore = 0;
        try{
            if(consume > MAX_CONSUME){
                consumeScore = 1;
            }else {
                consumeScore = consume / MAX_CONSUME ;
            }
        }catch(Exception e){
            logger.error(AvailField.CONSUME_SCORE_EXCEPTION,e);
        }
        return consumeScore;
    }

    /**
     * 计算酒店的价格分
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
     * 计算酒店的评价分
     * @param evaluateScore
     * @return
     */
    public double calcEvaluateScore(double evaluateScore){
        if((evaluateScore - MIN_EVASCORE) < 0.0001){
            return 0;
        }
        double consumeScore = 0;
        consumeScore = evaluateScore / MAX_EVASCORE ;
        return consumeScore;
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
