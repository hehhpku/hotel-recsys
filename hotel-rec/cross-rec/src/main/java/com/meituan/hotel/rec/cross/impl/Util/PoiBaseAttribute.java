package com.meituan.hotel.rec.cross.impl.Util;

/**
 * poi的基本属性定义
 * Created by zuolin on 15/11/6.
 */
public class PoiBaseAttribute {

    private int consume;                          //poi历史销量
    private double lowPrice;                      //poi最低价格
    private double highPrice;                     //poi最高价格
    private double distance;                      //poi到酒店的距离
    private double hotelTravelCorrScore;          //酒店poi与旅游poi的相关度得分
    private int evaluateCnt;                      //poi评价数目
    private double evaluateScore;                 //poi的平均得分
    private String lastEndDealDate;               //poi的deal最晚结束时间
    private String star ;                         //旅游星级

    /**
     * 构造方法，初始化属性类
     */
    public PoiBaseAttribute() {
        this.consume = 0;
        this.lowPrice = 0.0;
        this.highPrice = 0.0;
        this.distance = 0.0;
        this.hotelTravelCorrScore = 0.0;
        this.evaluateCnt = 0;
        this.evaluateScore = 0.0;
        this.star = "-1";
        this.lastEndDealDate = null;
    }

    public PoiBaseAttribute(int consume, double lowPrice,double highPrice,
                            int evaluateCnt, double evaluateScore,String lastEndDealDate ,String star){
        this.consume = consume;
        this.lowPrice = lowPrice;
        this.highPrice = highPrice;
        this.distance = 0.0;
        this.hotelTravelCorrScore = 0.0;
        this.evaluateCnt = evaluateCnt;
        this.evaluateScore = evaluateScore;
        this.lastEndDealDate = lastEndDealDate;
        this.star =star;
    }

    public int getConsume() {
        return consume;
    }

    public void setConsume(int consume) {
        this.consume = consume;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double nearHotelDistance) {
        this.distance = nearHotelDistance;
    }

    public double getHotelTravelCorrScore() {
        return hotelTravelCorrScore;
    }

    public void setHotelTravelCorrScore(double hotelTravelCorrScore) {
        this.hotelTravelCorrScore = hotelTravelCorrScore;
    }

    public int getEvaluateCnt() {
        return evaluateCnt;
    }

    public void setEvaluateCnt(int evaluateCnt) {
        this.evaluateCnt = evaluateCnt;
    }

    public double getEvaluateScore() {
        return evaluateScore;
    }

    public void setEvaluateScore(double evaluateScore) {
        this.evaluateScore = evaluateScore;
    }

    public String getLastDealDate() {
        return lastEndDealDate;
    }

    public void setLastDealDate(String lastDealDate) {
        this.lastEndDealDate = lastDealDate;
    }

    public String getStar() {
        return star;
    }

    public void setStar(String star) {
        this.star = star;
    }

    public String toString(){
        return star + "," + roundDouble(distance) + "," + consume + "," + roundDouble((lowPrice + highPrice)/2)
                + "," + hotelTravelCorrScore;
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

