package com.meituan.hotel.rec.service.log;

import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.HotelRecRequest;
import com.meituan.hotel.rec.thrift.HotelRecResponse;
import com.meituan.hotel.rec.thrift.RecServiceType;

import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 4/12/16
 * 日志收集
 */
public class RecLog {
    public static final Logger logger = RecUtils.getLogger(RecLog.class.getSimpleName());

    private Map<RecServiceType, AbstractLogger> loggerMap;

    @Resource(name = "simple-log-service")
    private SimpleLogger simpleLogger;

    public void printLog2Flume(RecRequest request, HotelRecRequest hotelRecRequest, HotelRecResponse response, JSONObject joLog, List<PoiEntry> poiEntryList){
        if (!loggerMap.containsKey(hotelRecRequest.getServiceType())){
            return;
        }

        AbstractLogger abstractLogger = simpleLogger;
        if (loggerMap.containsKey(request.getServiceType())) {
            abstractLogger = loggerMap.get(hotelRecRequest.getServiceType());
        }
        try{
            abstractLogger.printLog2Flume(request, hotelRecRequest, response, joLog, poiEntryList);
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("printLog2Flume()"), e);
        }
    }

    public void setLoggerMap(Map<RecServiceType, AbstractLogger> loggerMap) {
        this.loggerMap = loggerMap;
    }
}
