package com.meituan.hotel.rec.cross.impl.Util;



import java.util.Comparator;

/**
 * Created by zuolin on 15/11/3.
 */
public class TravelOrHotelPoiInfo implements Comparable<TravelOrHotelPoiInfo>, Comparator<TravelOrHotelPoiInfo> {
    private long poiId;                                  //poi的id
    private double lat;                                 //poi的纬度
    private double lng;                                 //poi的经度
    private int typeId;                                 //旅游的类别标志，368为景点
    private PoiBaseAttribute poiBaseAttribute;          //poi的基本属性

    private double poiScoreSum;                     //poi的综合评分之和,作为推荐的依据


    /**
     * 构造函数, 用poi最基本的信息
     */

    public TravelOrHotelPoiInfo() {
        this.poiId = 0;
        this.lat = 0;
        this.lng = 0;
        this.typeId = -1;
        this.poiBaseAttribute = new PoiBaseAttribute();
        this.poiScoreSum = 0;
    }

    public TravelOrHotelPoiInfo(long poiId, double lat, double lng, int typeId, PoiBaseAttribute poiBaseAttribute){
        this.poiId = poiId;
        this.lat = lat;
        this.lng = lng;
        this.typeId = typeId;
        this.poiBaseAttribute = poiBaseAttribute;
        this.poiScoreSum = 0;
    }

    /**
     * getter and setter
     */
    public long getPoiId() {
        return poiId;
    }
    public void setPoiId(long poiId) {
        this.poiId = poiId;
    }

    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public PoiBaseAttribute getPoiBaseAttribute() {
        return poiBaseAttribute;
    }
    public void setPoiBaseAttribute(PoiBaseAttribute poiBaseAttribute) {
        this.poiBaseAttribute = poiBaseAttribute;
    }

    public double getPoiScoreSum() {
        return poiScoreSum;
    }
    public void setPoiScoreSum(double poiScoreSum) {
        this.poiScoreSum = poiScoreSum;
    }

    @Override
    public String toString() {
        String poiInfoString = Long.toString(this.poiId);
        return poiInfoString;
    }

    /**
     * 利用Poi与用户的距离来比较,正排序
     * @param travelOrHotelPoiInfo
     * @return
     */
    @Override
    public int compareTo(TravelOrHotelPoiInfo travelOrHotelPoiInfo){
        if (this.poiBaseAttribute.getDistance()<
                travelOrHotelPoiInfo.poiBaseAttribute.getDistance())
            return -1;
        else if (this.poiBaseAttribute.getDistance() >
                travelOrHotelPoiInfo.getPoiBaseAttribute().getDistance())
            return 1;
        else
            return 0;
    }

    /**
     * 利用poi的综合评分来比较
     * @param poiInfo1
     * @param poiInfo2
     * @return
     */
    @Override
    public int compare(TravelOrHotelPoiInfo poiInfo1, TravelOrHotelPoiInfo poiInfo2) {
        if (poiInfo1.poiScoreSum > poiInfo2.poiScoreSum)
            return 1;
        else if (poiInfo1.poiScoreSum < poiInfo2.poiScoreSum)
            return -1;
        else
            return 0;
    }
}
