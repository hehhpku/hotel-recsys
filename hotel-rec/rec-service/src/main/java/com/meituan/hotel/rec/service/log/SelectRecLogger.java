package com.meituan.hotel.rec.service.log;


import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.thrift.*;

import org.json.JSONObject;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hehuihui on 4/11/16
 */
@Service("select-rec-log-service")
public class SelectRecLogger extends AbstractLogger {
    public static final Logger logScribe = LoggerFactory.getLogger("hotel_rec_agg");

    @Override
    public Logger getLogger() {
        return logScribe;
    }

    @Override
    public Map<String, String> getInfoMap(RecRequest request, HotelRecRequest hotelRecRequest, HotelRecResponse response, JSONObject joLog){

        Map<String, String> map = new HashMap<String, String>();
        map.put("checkin_city", String.valueOf(request.getChannelCityId()));

        return map;
    }


}
