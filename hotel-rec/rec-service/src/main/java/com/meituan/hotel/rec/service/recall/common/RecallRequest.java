package com.meituan.hotel.rec.service.recall.common;

import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.thrift.*;

import java.util.*;

/**
 * Author: hehuihui@meituan.com
 * Date: 3/15/16
 * desc: 召回环节的请求
 */
public class RecallRequest {
    //频道内的城市id及名称
    private int channelCityId;
    private String channelCityName;
    //poi所在的城市id列表
    private List<Integer> poiCityIdList = new ArrayList<Integer>();
    private int userLocationCityId;

    //请求住宿的位置及用户的位置
    private Location requestLocation;
    private Location userLocation;
    private long userId;

    //搜索的query
    private String query;

    //过滤的poi集合
    private Set<Integer> blackPoiSet = new HashSet<Integer>();
    private List<Integer> poiOnShow = new ArrayList<Integer>();

    private RecServiceType type;


    public int getChannelCityId() {
        return channelCityId;
    }

    public void setChannelCityId(int channelCityId) {
        this.channelCityId = channelCityId;
    }

    public String getChannelCityName() {
        return channelCityName;
    }

    public void setChannelCityName(String channelCityName) {
        this.channelCityName = channelCityName;
    }

    public List<Integer> getPoiCityIdList() {
        return poiCityIdList;
    }

    public void setPoiCityIdList(List<Integer> poiCityIdList) {
        this.poiCityIdList = poiCityIdList;
    }

    public Location getRequestLocation() {
        return requestLocation;
    }

    public void setRequestLocation(Location requestLocation) {
        this.requestLocation = requestLocation;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public int getUserLocationCityId() {
        return userLocationCityId;
    }

    public void setUserLocationCityId(int userLocationCityId) {
        this.userLocationCityId = userLocationCityId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Set<Integer> getBlackPoiSet() {
        return blackPoiSet;
    }

    public List<Integer> getPoiOnShow() {
        return poiOnShow;
    }

    public RecServiceType getType() {
        return type;
    }

    public void setType(RecServiceType type) {
        this.type = type;
    }

    public long getUserId() {
        return userId;
    }

    /**
     * 初始化召回的请求信息
     * @param request
     */
    public RecallRequest(RecRequest request){
        this.channelCityId = request.getChannelCityId();
        this.channelCityName = request.getCityName();
        this.poiCityIdList = request.getPoiCityIdList();
        this.userLocationCityId = request.getUserLocationCityId();
        this.requestLocation = request.getRequestLocation();
        this.userLocation = request.getUserLocation();
        this.query = request.getQuery();
        this.poiOnShow = request.getPoiOnShowList();
        this.blackPoiSet = new HashSet<Integer>(poiOnShow);
        this.type = request.getServiceType();
        this.userId = request.getUserId();
    }
}
