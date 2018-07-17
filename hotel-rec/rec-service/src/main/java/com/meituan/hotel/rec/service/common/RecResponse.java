package com.meituan.hotel.rec.service.common;

import com.meituan.hotel.rec.thrift.RecServiceStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: hehuihui@meituan.com Date: 3/15/16
 */
public class RecResponse {
    private List<PoiEntry> poiEntryList;
    private String strategy;
    private RecServiceStatus serviceStatus;

    public List<PoiEntry> getPoiEntryList() {
        return poiEntryList;
    }

    public void setPoiEntryList(List<PoiEntry> poiEntryList) {
        this.poiEntryList = poiEntryList;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public RecServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(RecServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public static RecResponse getErrorResponse(){
        RecResponse recResponse = new RecResponse();
        recResponse.setPoiEntryList(new ArrayList<PoiEntry>());
        recResponse.setServiceStatus(RecServiceStatus.ERROR);
        return recResponse;
    }
}
