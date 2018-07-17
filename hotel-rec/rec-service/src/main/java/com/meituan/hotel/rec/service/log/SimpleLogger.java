package com.meituan.hotel.rec.service.log;

import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.thrift.HotelRecRequest;
import com.meituan.hotel.rec.thrift.HotelRecResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hehuihui on 5/18/16
 */
@Service("simple-log-service")
public class SimpleLogger extends AbstractLogger{
    public static final Logger logScribe = LoggerFactory.getLogger("hotel_rec_agg");


    @Override
    public Logger getLogger() {
        return logScribe;
    }

    @Override
    public Map<String, String> getInfoMap(RecRequest request, HotelRecRequest hotelRecRequest, HotelRecResponse response, JSONObject joLog) {
        return new HashMap<String, String>();
    }
}
