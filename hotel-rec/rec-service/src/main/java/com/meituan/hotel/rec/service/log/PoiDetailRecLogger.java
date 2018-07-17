package com.meituan.hotel.rec.service.log;

import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.utils.RecDateUtils;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.service.utils.TransformationUtils;
import com.meituan.hotel.rec.thrift.HotelRecRequest;
import com.meituan.hotel.rec.thrift.HotelRecResponse;
import com.meituan.hotel.rec.thrift.UserRecInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hehuihui on 4/11/16
 */
@Service("poi-detail-rec-log-service")
public class PoiDetailRecLogger extends AbstractLogger {
    public static final Logger logScribe = LoggerFactory.getLogger("poiDetailRecLogScribe");

    @Override
    public Logger getLogger() {
        return logScribe;
    }

    @Override
    public Map<String, String> getInfoMap(RecRequest request, HotelRecRequest hotelRecRequest, HotelRecResponse response, JSONObject joLog) {
        Map<String, String> map = new HashMap<String, String>();
        List<String> keys = Arrays.asList(
                "strategy_recall"
        );
        for (String key: keys){
            map.put(key, getOrElse(joLog, key, "0"));
        }

        map.put("poi_city_list", String.valueOf(request.getPoiCityIdList()));
        map.put("location_city", RecDistanceUtils.location2String(request.getUserLocation()));
        if (CollectionUtils.isNotEmpty(hotelRecRequest.getPoiOnShow())){
            map.put("poi_id", String.valueOf(hotelRecRequest.getPoiOnShow().get(0)));
            map.put("poi_location", RecDistanceUtils.location2String(request.getRequestLocation()));
        }
        map.put("rec_time", RecDateUtils.getCurrentTimeString(null));
        if (response != null) {
            map.put("rec_poi_list", TransformationUtils.getPoiIdListFromPoiRecInfo(response.getPoiRecList()).toString());
        }
        return map;
    }

    public static String getOrElse(JSONObject jo, String key, String defaultValue){
        if (jo == null || !jo.has(key)){
            return String.valueOf(defaultValue);
        } else {
            try {
                return String.valueOf(jo.get(key));
            } catch (Exception e){
                logger.error(RecUtils.getErrorString("getOrElse()"), e);
                return String.valueOf(defaultValue);
            }
        }
    }
}
