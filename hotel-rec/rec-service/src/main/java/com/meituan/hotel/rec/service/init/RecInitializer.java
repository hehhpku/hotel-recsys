package com.meituan.hotel.rec.service.init;

import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.thrift.HotelRecRequest;
import com.meituan.hotel.rec.thrift.RecServiceType;
import com.meituan.jmonitor.JMonitor;

import org.json.JSONObject;

import java.util.Map;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 4/13/16
 */
public class RecInitializer {
    @Resource
    private DefaultInitializer defaultInitializer;

    private Map<RecServiceType, AbstractInitializer> initializerMap;

    public RecRequest initRequest(HotelRecRequest hotelRecRequest, JSONObject joLog){
        long start = System.currentTimeMillis();

        RecServiceType serviceType = hotelRecRequest.getServiceType();
        AbstractInitializer initializer = defaultInitializer;
        if (initializerMap.containsKey(serviceType)){
            initializer = initializerMap.get(serviceType);
        }
        RecRequest request= initializer.initRequest(hotelRecRequest, joLog);

        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.INIT_TIME, timeCost);

        return request;
    }

    public void setInitializerMap(Map<RecServiceType, AbstractInitializer> initializerMap) {
        this.initializerMap = initializerMap;
    }
}
