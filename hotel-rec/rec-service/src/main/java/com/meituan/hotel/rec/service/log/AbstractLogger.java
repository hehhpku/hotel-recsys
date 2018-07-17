package com.meituan.hotel.rec.service.log;

import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.constants.Constants;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.service.utils.TransformationUtils;
import com.meituan.hotel.rec.thrift.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.*;

/**
 * Created by hehuihui on 4/11/16
 * 打印日志到flume，收集到hive中
 */
public abstract class AbstractLogger {

    public static final Logger logger = RecUtils.getLogger(AbstractLogger.class.getSimpleName());

    public void printLog2Flume(RecRequest request, HotelRecRequest hotelRecRequest, HotelRecResponse response, JSONObject joLog, List<PoiEntry> poiEntryList){
        Logger logger = getLogger();
        StringBuilder sb = new StringBuilder();
        Map<String, String> map = getInfoMap(request, hotelRecRequest, response, joLog);
        Map<String, String> basicInfoMap = getBasicCommonInfoMap(request, hotelRecRequest, response, joLog, poiEntryList);
        map.putAll(basicInfoMap);

        if (logger == null || MapUtils.isEmpty(map)){
            return;
        }

        for (Map.Entry<String, String> e: map.entrySet()){
            sb.append(e.getKey()).append(Constants.SEPERATOR_EQ).append(e.getValue()).append(Constants.SEPERATOR_SPACE);
        }
        logger.info(sb.toString());
    }

    /**
     * 获取logger scribe
     * @return
     */
    public abstract Logger getLogger();

    /**
     * 统一处理各个推荐服务需要的基本信息，大多与用户和埋点相关的属性
     * @param request
     * @param hotelRecRequest
     * @param response
     * @param joLog
     * @return
     */
    public static Map<String, String> getBasicCommonInfoMap(RecRequest request, HotelRecRequest hotelRecRequest, HotelRecResponse response, JSONObject joLog, List<PoiEntry> poiEntryList){
        Map<String, String> map = new HashMap<String, String>();

        //与用户信息相关
        if (hotelRecRequest.isSetUserInfo()){
            UserRecInfo userRecInfo = hotelRecRequest.getUserInfo();
            map.put("uuid", userRecInfo.getUuid());
            map.put("client", userRecInfo.getClientType());
            map.put("version", userRecInfo.getAppVersion());
            map.put("accommodation_type", userRecInfo.getAccType().name());
            map.put("checkin_date", String.valueOf( userRecInfo.getCheckInDate()));
            map.put("checkout_date", String.valueOf( userRecInfo.getCheckOutDate()));
            map.put("user_location", RecDistanceUtils.location2String(userRecInfo.getUserLocation()));
            map.put("location_city", String.valueOf(userRecInfo.getUserLocationCityId()));

            List<String> utmKeys = Arrays.asList(
                    "utm_campaign",
                    "utm_content",
                    "utm_medium",
                    "utm_source",
                    "utm_term"
            );
            Map<String, String> extraMap = userRecInfo.getExtraDataMap();
            for (String key: utmKeys){
                map.put(key, MapUtils.getObject(extraMap, key, "NULL").toString());
            }
        }
        map.put("user_id", String.valueOf(request.getUserId()));

        //推荐类型
        map.put("request_type", hotelRecRequest.getServiceType().name());

        //请求的json
        map.put("request", RecUtils.getJSONString(hotelRecRequest));

        //收集特征信息
        map.put("feature_json", getFeatureString(poiEntryList));

        //与返回结果相关
        if (response != null) {
            map.put("strategy", response.getStrategy());
            map.put("return_count", String.valueOf(response.getPoiRecListSize()));
            map.put("rec_list", TransformationUtils.getPoiIdListFromPoiRecInfo(response.getPoiRecList()).toString());
        }

        //与埋点相关
        List<String> keys = Arrays.asList(
                "global_id",
                "st_id"
        );
        for (String key: keys){
            map.put(key, PoiDetailRecLogger.getOrElse(joLog, key, "0"));
        }

        //用户请求的住宿位置
        map.put("request_location", RecDistanceUtils.location2String(request.getRequestLocation()));

        //展示偏移量
        map.put("offset", String.valueOf(hotelRecRequest.getPoiOnShowSize()));

        return map;
    }

    /**
     * 获取一些线上的特征，例如：poi距离用户请求住宿位置的距离，打印在日志中
     * @param poiEntryList
     * @return
     * @throws JSONException
     */
    public static String getFeatureString(List<PoiEntry> poiEntryList){
        JSONObject featureJO = new JSONObject();
        if (poiEntryList != null && CollectionUtils.isNotEmpty(poiEntryList)) {
            try {
                for (PoiEntry entry : poiEntryList) {
                    JSONObject detailJO = new JSONObject();
                    detailJO.put(Constants.DISTANCE_TO_REQ, entry.getDistanceToRequest());
                    detailJO.put(Constants.CORR_SCORE, entry.getCorrScore());
                    detailJO.put(Constants.DIS_SCORE, entry.getDistanceScore());
                    detailJO.put(Constants.MARK_SCORE, entry.getMarkScore());
                    detailJO.put(Constants.RAW_SCORE, entry.getRawRankScore());
                    detailJO.put(Constants.PRICE_SCORE, entry.getPriceScore());
                    featureJO.put(String.valueOf(entry.getPoiId()), detailJO);
                }
            } catch (Exception e){
                logger.error(RecUtils.getErrorString("getFeatureString"), e);
            }
        }
        return featureJO.toString();
    }

    /**
     * 获取数据map, key->value 都是string， 打印成key=value格式
     * @param request
     * @param hotelRecRequest
     * @param response
     * @param joLog
     * @return
     */
    public abstract Map<String, String> getInfoMap(RecRequest request, HotelRecRequest hotelRecRequest, HotelRecResponse response, JSONObject joLog);
}
