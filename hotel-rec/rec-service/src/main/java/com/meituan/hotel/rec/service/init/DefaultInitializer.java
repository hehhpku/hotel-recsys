package com.meituan.hotel.rec.service.init;

import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.thrift.HotelRecRequest;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * Created by hehuihui on 4/13/16
 */
@Service("default-initializer")
public class DefaultInitializer extends AbstractInitializer {
    @Override
    public void addInfo2Req(RecRequest request, HotelRecRequest hotelRecRequest, JSONObject joLog) {

    }
}
