package com.meituan.hotel.rec.cross.impl.Util;

import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * 收集请求和返回的策略定义
 * LogCollection.java
 * Created by zuolin on 15/11/6.
 */
public class LogCollection {

    private static final Logger logger = LoggerFactory.getLogger(LogCollection.class);

    /**
     * 收集请求
     * @param request
     * @param jsod
     */
    public static void CollectReq(JSONObject jsod, CrossRecRequest request){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject reqJSON = new JSONObject();

        try{
            if(request.isSetUserId()){
                reqJSON.put("userid", request.getUserId());
            }
            else {
                reqJSON.put("userid",-1);
            }
            if(request.getRecSceneType() != null){
                reqJSON.put("recSceneType",request.getRecSceneType());
            }
            else{
                reqJSON.put("recSceneType","NULL");
            }
            if(request.isSetStrategy()){
                reqJSON.put("strategy",request.getStrategy());
            }else{
                reqJSON.put("strategy","default");
            }
            if(request.isSetUserLat()){
                reqJSON.put("userLat",request.getUserLat());
            }else{
                reqJSON.put("userLat",-1);
            }
            if(request.isSetUserLng()){
                reqJSON.put("userLng",request.getUserLng());
            }else{
                reqJSON.put("userLng",-1);
            }
            if(request.isSetUserOrderCityid()){
                reqJSON.put("userOrderCityid",request.getUserOrderCityid());
            }else{
                reqJSON.put("userOrderCityid",-1);
            }
            if(request.isSetUserResCityId()){
                reqJSON.put("userResCityId",request.getUserResCityId());
            }else{
                reqJSON.put("userResCityId",-1);
            }
            if(request.getHotelOrderInfo() != null){
                reqJSON.put("hotelOrderInfo",request.getHotelOrderInfo());
            }else{
                reqJSON.put("hotelOrderInfo","NULL");
            }
            if(request.getTravelOrderInfo() != null){
                reqJSON.put("travelOrderInfo",request.getTravelOrderInfo());
            }else{
                reqJSON.put("travelOrderInfo","NULL");
            }
            if(request.getTicketOrderInfo() != null){
                reqJSON.put("ticketOrderInfo",request.getTicketOrderInfo());
            }else{
                reqJSON.put("ticketOrderInfo","NULL");
            }
            if(request.isSetUuid()){
                reqJSON.put("uuid",request.getUuid());
            }else{
                reqJSON.put("uuid","NULL");
            }
            if(request.isSetClientType()){
                reqJSON.put("clientType",request.getClientType());
            }else{
                reqJSON.put("clientType","NULL");
            }
            if(request.isSetAppVersion()){
                reqJSON.put("appVersion",request.getAppVersion());
            }else{
                reqJSON.put("appVersion","NULL");
            }
            if(request.isSetUnusedTravelPoiid()){
                reqJSON.put("unusedTravelPoiid",request.getUnusedTravelPoiid());
            }else{
                reqJSON.put("unusedTravelPoiid","NULL");
            }

            jsod.put("request",reqJSON);
            jsod.put("time", sdf.format(new Date()));
        }catch (Exception e){
            logger.error(AvailField.REQLOG_COLLECTION_EXCEPTION, e);
        }

    }

    /**
     * 收集返回日志
     * @param jsod
     * @param response
     */
    public static void CollectRes(JSONObject jsod, CrossRecResponse response ,String recHotelOrTravel) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject resJSON = new JSONObject();

        try {
            if (response != null) {
                if (response.getPoiArrayList() != null) {
                    resJSON.put("poiArrayList", response.getPoiArrayList());
                } else {
                    resJSON.put("poiArrayList", "NULL");
                }
                if (response.isSetStrategy()) {
                    resJSON.put("strategy", response.getStrategy());
                } else {
                    resJSON.put("strategy", "NULL");
                }
                if (response.isSetStatus()) {
                    resJSON.put("status", response.getStatus());
                } else {
                    resJSON.put("status", "NULL");
                }
                if (response.isSetTotalNumOfPoi()) {
                    resJSON.put("totalNumOfPoi", response.getTotalNumOfPoi());
                } else {
                    resJSON.put("totalNumOfPoi", -1);
                }
                jsod.put("response" + "_" + recHotelOrTravel, resJSON);
                jsod.put("time", sdf.format(new Date()));
            }
        } catch (Exception e) {
            logger.error(AvailField.RESLOG_COLLECTION_EXCEPTION, e);
        }
    }

    /**
     * 打印存储的日志内容
     * @param jsod
     */
    public static void printJSON(JSONObject jsod){
        List<String> stringList = getJSONKeys(jsod);
        String printString = "";
        try{
            for (String s : stringList){
                printString += s + ":" + jsod.getString(s) + "\t";
            }
        }catch (Exception e){
            logger.error(AvailField.JSON_PRINT_EXCEPTION, e);
        }
        logger.info(printString);
    }

    /**
     * 获取json的key
     * @param jsod
     * @return
     */
    public static List<String> getJSONKeys(JSONObject jsod){
        List<String> retKeys = new ArrayList<String>();
        Iterator iter = jsod.keys();
        while(iter.hasNext()){
            String key = iter.next().toString();
            if ("request".equals(key)){
                retKeys.add(0,key);
            }else if ("response_travel".equals(key)){
                retKeys.add(0,key);
            }else if("response_hotel".equals(key)){
                retKeys.add(0,key);
            }else{
                retKeys.add(key);
            }
        }
        return retKeys;
    }

}
