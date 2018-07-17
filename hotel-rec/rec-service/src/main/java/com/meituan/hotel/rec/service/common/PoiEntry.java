package com.meituan.hotel.rec.service.common;

import com.meituan.hotel.rec.data.HotelPoiBasicInfo;
import com.meituan.hotel.rec.service.constants.Constants;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.service.utils.TransformationUtils;
import com.meituan.hotel.rec.thrift.DealRecInfo;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.hotel.rec.thrift.PoiRecInfo;
import com.meituan.service.mobile.sinai.client.model.PoiModel;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: hehuihui@meituan.com Date: 3/15/16
 */
public class PoiEntry {
    private static final Logger logger = RecUtils.getLogger(PoiEntry.class.getSimpleName());

    private int poiId;
    private double distanceToUser = -1.0;
    private double distanceToRequest = -1.0;
    private int distanceToReqUPS = Integer.MAX_VALUE;
    private PoiSource source = PoiSource.NEAR_BY;

    private int brandId;
    private double lowestPrice;
    private double avgScore;

    private int markNumbers;
    private int sales;
    private int hotelStar = Constants.LOW_HOTEL_STAR;

    private double corrScore = 0.0;
    private double salesScore = 0.0;
    private double priceScore = 0.0;
    private double distanceScore = 0.0;
    private double markScore = 0.0;

    private double rawRankScore;
    private double rerankScore;

    private boolean isSetFeature = false;

    private Location location;
    private List<Integer> poiCityIds;
    private List<Integer> dealIdList;
    private String ct_poi;
    private String stid;

    public PoiEntry(int poiId, double distanceToUser, double distanceToRequest) {
        this.poiId = poiId;
        this.distanceToUser = distanceToUser;
        this.distanceToRequest = distanceToRequest;
    }

    /**
     * 来自rec-data的HotelPoiBasicInfo初始化PoiEntry的信息
     * @param info
     */
    public PoiEntry(HotelPoiBasicInfo info){
        this.poiId = info.getPoiId();
        this.distanceToRequest = info.getDistanceToUser();
        this.avgScore = info.getAvgScore();
        this.lowestPrice = info.getLowestPrice();
        this.brandId = info.getBrandId();
        this.markNumbers = info.getMarkNumber();
        this.sales = info.getHistoryCouponCount();
        this.poiCityIds = info.getCityId();
        this.dealIdList = info.getDealIdList();
        this.isSetFeature = true;
        this.hotelStar = info.getHotelStar();
    }

    public PoiEntry(PoiBasicEntry e){
        this.poiId = e.getPoiId();
        this.distanceToRequest = e.getDistanceToRequest();
        this.distanceToUser = e.getDistanceToUser();
    }

    /**
     * 来自sinai的PoiModel类初始化poiEntry的信息
     * @param poiModel
     */
    public PoiEntry(PoiModel poiModel){
        try {
            this.poiId = poiModel.getId();
            this.avgScore = poiModel.getAvgscore();
            this.lowestPrice = poiModel.getLowestprice();
            this.brandId = poiModel.getBrandId();
            this.markNumbers = poiModel.getMarknumbers();
            this.sales = poiModel.getHistoryCouponCount();
            this.location = new Location(poiModel.getLatitude(), poiModel.getLongitude());
            this.poiCityIds = poiModel.getCityIds();
            this.dealIdList = TransformationUtils.parseIntegerString(poiModel.getdIds(), ",");
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("PoiEntry"), e);
        }
        this.isSetFeature = true;
    }

    /**
     * 将PoiEntry转化为对外传输用的PoiRecInfo
     * @return
     */
    public PoiRecInfo convert2PoiRecInfo(){
        PoiRecInfo poiRecInfo = new PoiRecInfo();
        poiRecInfo.setPoiId(this.poiId);
        poiRecInfo.setCt_poi(this.ct_poi);
        poiRecInfo.setDistanceToRequest(this.distanceToRequest);
        poiRecInfo.setDistanceToUser(-1.0);
        //deal 信息
        List<DealRecInfo> dealRecInfoList = new ArrayList<DealRecInfo>();
        for (int dealId: dealIdList){
            DealRecInfo dealRecInfo = new DealRecInfo(dealId);
            dealRecInfo.setSt_id(this.stid);
            dealRecInfoList.add(dealRecInfo);
        }
        poiRecInfo.setDealList(dealRecInfoList);
        return poiRecInfo;
    }

    public void updateInfo(PoiEntry e){
        this.avgScore = e.avgScore;
        this.lowestPrice = e.lowestPrice;
        this.brandId = e.brandId;
        this.markNumbers = e.markNumbers;
        this.sales = e.sales;
        this.location = e.location;
        this.poiCityIds = e.poiCityIds;
        this.dealIdList = e.dealIdList;
        this.isSetFeature = true;
    }

    public PoiEntry() {
    }

    public PoiEntry(int poiId) {
        this.poiId = poiId;
    }

    public int getPoiId() {
        return poiId;
    }

    public void setPoiId(int poiId) {
        this.poiId = poiId;
    }

    public double getDistanceToUser() {
        return distanceToUser;
    }

    public void setDistanceToUser(double distanceToUser) {
        this.distanceToUser = distanceToUser;
    }

    public double getDistanceToRequest() {
        return distanceToRequest;
    }

    public void setDistanceToRequest(double distanceToRequest) {
        this.distanceToRequest = distanceToRequest;
    }

    public double getCorrScore() {
        return corrScore;
    }

    public void setCorrScore(double corrScore) {
        this.corrScore = corrScore;
    }

    public PoiSource getSource() {
        return source;
    }

    public void setSource(PoiSource source) {
        this.source = source;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public double getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(double lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(double avgScore) {
        this.avgScore = avgScore;
    }

    public int getMarkNumbers() {
        return markNumbers;
    }

    public void setMarkNumbers(int markNumbers) {
        this.markNumbers = markNumbers;
    }

    public int getSales() {
        return sales;
    }

    public void setSales(int sales) {
        this.sales = sales;
    }

    public double getRawRankScore() {
        return rawRankScore;
    }

    public void setRawRankScore(double rawRankScore) {
        this.rawRankScore = rawRankScore;
    }

    public boolean isSetFeature() {
        return isSetFeature;
    }

    public void setIsSetFeature(boolean isSetFeature) {
        this.isSetFeature = isSetFeature;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Integer> getPoiCityIds() {
        return poiCityIds;
    }

    public void setPoiCityIds(List<Integer> poiCityIds) {
        this.poiCityIds = poiCityIds;
    }

    public List<Integer> getDealIdList() {
        return dealIdList;
    }

    public void setDealIdList(List<Integer> dealIdList) {
        this.dealIdList = dealIdList;
    }

    public int getDistanceToRequestUPS() {
        if (this.distanceToReqUPS == Integer.MAX_VALUE){
            this.distanceToReqUPS = (int)distanceToRequest / 3000;
            if (this.distanceToReqUPS > 5)
                this.distanceToReqUPS = 5;
        }

        return this.distanceToReqUPS;
    }

    public String getCt_poi() {
        return ct_poi;
    }

    public void setCt_poi(String ct_poi) {
        this.ct_poi = ct_poi;
    }

    public double getRerankScore() {
        return rerankScore;
    }

    public void setRerankScore(double rerankScore) {
        this.rerankScore = rerankScore;
    }

    public int getHotelStar() {
        return hotelStar;
    }

    public void setHotelStar(int hotelStar) {
        this.hotelStar = hotelStar;
    }

    public double getDistanceScore() {
        return distanceScore;
    }

    public void setDistanceScore(double distanceScore) {
        this.distanceScore = distanceScore;
    }

    public int getDistanceToReqUPS() {
        return distanceToReqUPS;
    }

    public void setDistanceToReqUPS(int distanceToReqUPS) {
        this.distanceToReqUPS = distanceToReqUPS;
    }

    public static Logger getLogger() {
        return logger;
    }

    public double getMarkScore() {
        return markScore;
    }

    public void setMarkScore(double markScore) {
        this.markScore = markScore;
    }

    public double getPriceScore() {
        return priceScore;
    }

    public void setPriceScore(double priceScore) {
        this.priceScore = priceScore;
    }

    public double getSalesScore() {
        return salesScore;
    }

    public void setSalesScore(double salesScore) {
        this.salesScore = salesScore;
    }

    public String getStid() {
        return stid;
    }

    public void setStid(String stid) {
        this.stid = stid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PoiEntry entry = (PoiEntry) o;

        return poiId == entry.poiId;

    }

    @Override
    public int hashCode() {
        return poiId;
    }

    @Override
    public String toString() {
        return "poiId=" + poiId +
                ", distanceToRequest=" + distanceToRequest;
    }
}
