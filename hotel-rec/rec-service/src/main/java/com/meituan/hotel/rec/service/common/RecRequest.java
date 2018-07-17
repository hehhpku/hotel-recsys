package com.meituan.hotel.rec.service.common;

import com.meituan.hotel.rec.service.constants.Constants;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.thrift.AccommodationType;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.hotel.rec.thrift.RecServiceType;
import com.meituan.hotel.rec.thrift.SortingMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hehuihui on 3/16/16
 */
public class RecRequest {
    private int channelCityId = 0;
    private int checkInDate;
    private int checkOutDate;
    private int appCityId = 0;
    private int userLocationCityId = 0;
    private String clientType = "";
    private String appVersion = "0";
    private SortingMethod sortingMethod = SortingMethod.SMART;
    private int offset = 0;

    private String uuid = "";
    private long userId = 0L;
    private AccommodationType accommodationType = AccommodationType.DR;

    private Location userLocation = RecDistanceUtils.getNullLocation();
    private Location requestLocation = RecDistanceUtils.getNullLocation();

    private String strategy = "";
    private RecServiceType serviceType;
    private List<Integer> poiOnShowList = new ArrayList<Integer>();

    //搜索附加请求参数
    private String query = "";
    private String cityName = "";
    private int sceneId;
    private double searchPoiPrice = 200;
    private Location baiduLocation = null;

    //筛选附加附加参数
    private List<Integer> roomTypeList = new ArrayList<Integer>();
    private List<Integer> hotelType = new ArrayList<Integer>();
    private List<Integer> brandId = new ArrayList<Integer>();
    private double priceLow;
    private double priceHigh;

    //poi详情页附加参数
    private List<Integer> poiCityIdList;

    public int getChannelCityId() {
        return channelCityId;
    }

    public void setChannelCityId(int channelCityId) {
        this.channelCityId = channelCityId;
    }

    public int getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(int checkInDate) {
        this.checkInDate = checkInDate;
    }

    public int getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(int checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getAppCityId() {
        return appCityId;
    }

    public void setAppCityId(int appCityId) {
        this.appCityId = appCityId;
    }

    public int getUserLocationCityId() {
        return userLocationCityId;
    }

    public void setUserLocationCityId(int userLocationCityId) {
        this.userLocationCityId = userLocationCityId;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public SortingMethod getSortingMethod() {
        return sortingMethod;
    }

    public void setSortingMethod(SortingMethod sortingMethod) {
        this.sortingMethod = sortingMethod;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public AccommodationType getAccommodationType() {
        return accommodationType;
    }

    public void setAccommodationType(AccommodationType accommodationType) {
        this.accommodationType = accommodationType;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public Location getRequestLocation() {
        return requestLocation;
    }

    public void setRequestLocation(Location requestLocation) {
        this.requestLocation = requestLocation;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public RecServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(RecServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public List<Integer> getPoiOnShowList() {
        return poiOnShowList;
    }

    public void setPoiOnShowList(List<Integer> poiOnShowList) {
        this.poiOnShowList = poiOnShowList;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public List<Integer> getRoomTypeList() {
        return roomTypeList;
    }

    public void setRoomTypeList(List<Integer> roomTypeList) {
        this.roomTypeList = roomTypeList;
    }

    public List<Integer> getHotelType() {
        return hotelType;
    }

    public void setHotelType(List<Integer> hotelType) {
        this.hotelType = hotelType;
    }

    public List<Integer> getBrandId() {
        return brandId;
    }

    public void setBrandId(List<Integer> brandId) {
        this.brandId = brandId;
    }

    public double getPriceLow() {
        return priceLow;
    }

    public void setPriceLow(double priceLow) {
        this.priceLow = priceLow;
    }

    public double getPriceHigh() {
        return priceHigh;
    }

    public void setPriceHigh(double priceHigh) {
        this.priceHigh = priceHigh;
    }

    public List<Integer> getPoiCityIdList() {
        return poiCityIdList;
    }

    public void setPoiCityIdList(List<Integer> poiCityIdList) {
        this.poiCityIdList = poiCityIdList;
    }

    public double getSearchPoiPrice() {
        return searchPoiPrice;
    }

    public void setSearchPoiPrice(double searchPoiPrice) {
        this.searchPoiPrice = searchPoiPrice;
    }

    public Location getBaiduLocation() {
        return baiduLocation;
    }

    public void setBaiduLocation(Location baiduLocation) {
        this.baiduLocation = baiduLocation;
    }

    @Override
    public String toString() {
        return "RecRequest{" +
                "channelCityId=" + channelCityId +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", appCityId=" + appCityId +
                ", userLocationCityId=" + userLocationCityId +
                ", clientType='" + clientType + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", sortingMethod=" + sortingMethod +
                ", offset=" + offset +
                ", uuid='" + uuid + '\'' +
                ", userId=" + userId +
                ", accommodationType=" + accommodationType +
                ", userLocation=" + userLocation +
                ", requestLocation=" + requestLocation +
                ", strategy='" + strategy + '\'' +
                ", serviceType=" + serviceType +
                ", poiOnShowList=" + poiOnShowList +
                ", query='" + query + '\'' +
                ", cityName='" + cityName + '\'' +
                ", sceneId=" + sceneId +
                ", searchPoiPrice=" + searchPoiPrice +
                ", roomTypeList=" + roomTypeList +
                ", hotelType=" + hotelType +
                ", brandId=" + brandId +
                ", priceLow=" + priceLow +
                ", priceHigh=" + priceHigh +
                ", poiCityIdList=" + poiCityIdList +
                '}';
    }
}
