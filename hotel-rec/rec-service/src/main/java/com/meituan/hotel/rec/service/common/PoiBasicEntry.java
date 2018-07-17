package com.meituan.hotel.rec.service.common;

/**
 * Created by hehuihui on 3/15/16
 */
public class PoiBasicEntry {
    private int poiId;
    private double distanceToUser = -1.0;
    private double distanceToRequest = -1.0;

    public PoiBasicEntry(int poiId, double distanceToUser, double distanceToRequest) {
        this.poiId = poiId;
        this.distanceToUser = distanceToUser;
        this.distanceToRequest = distanceToRequest;
    }

    public PoiBasicEntry(int poiId, double distanceToRequest) {
        this.poiId = poiId;
        this.distanceToRequest = distanceToRequest;
    }

    public double getDistanceToRequest() {
        return distanceToRequest;
    }

    public int getPoiId() {
        return poiId;
    }

    public double getDistanceToUser() {
        return distanceToUser;
    }
}
