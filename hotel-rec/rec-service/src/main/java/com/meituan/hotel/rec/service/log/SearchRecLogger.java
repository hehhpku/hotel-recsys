package com.meituan.hotel.rec.service.log;

import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.*;

import org.json.JSONObject;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hehuihui on 4/11/16
 */
@Service("search-rec-log-service")
public class SearchRecLogger extends AbstractLogger {
    public static final Logger logScribe = LoggerFactory.getLogger("hotel_rec_agg");

    @Override
    public Logger getLogger() {
        return logScribe;
    }

    @Override
    public Map<String, String> getInfoMap(RecRequest request, HotelRecRequest hotelRecRequest, HotelRecResponse response, JSONObject joLog) {
        Map<String, String> map = new HashMap<String, String>();

        try {
            JSONObject extraMsgJO = new JSONObject();
            SearchRecExtraMsg msg = hotelRecRequest.getSearchRecMsg();
            if (msg != null) {
                extraMsgJO.put("query", msg.getQuery());
                extraMsgJO.put("city_name", msg.getCityName());
                extraMsgJO.put("scene_id", msg.getSceneId());
                extraMsgJO.put("intention_type", msg.getIntentionType());
                extraMsgJO.put("identified_location", RecDistanceUtils.location2String(msg.getIdentifiedLocation()));
            }
            map.put("extra_msg", extraMsgJO.toString());
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("getInfoMap()"), e);
        }
        map.put("checkin_city", String.valueOf(request.getChannelCityId()));

        return map;
    }
}
