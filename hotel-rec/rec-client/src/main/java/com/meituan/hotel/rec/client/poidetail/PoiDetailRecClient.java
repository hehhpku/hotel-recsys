package com.meituan.hotel.rec.client.poidetail;

import com.meituan.hotel.rec.client.HotelRecClient;
import com.meituan.hotel.rec.client.common.RecMapUtils;
import com.meituan.hotel.rec.client.common.Utils;
import com.meituan.hotel.rec.poidetail.*;
import com.meituan.hotel.rec.thrift.*;

import org.apache.thrift.TException;

import java.util.*;

/**
 * Author: jiangweisen,jiangweisen@meituan.com Date: 2/1/16
 */
public class PoiDetailRecClient {

    private HotelRecClient hotelRecClient;

    public void setHotelRecClient(HotelRecClient hotelRecClient) {
        this.hotelRecClient = hotelRecClient;
    }

    /**
     * poi详情页推荐服务接口
     * @param poiDetailRecRequest 请求
     * @return 服务响应结构
     * @throws TException
     */
    @Deprecated
    public PoiDetailRecResponse recPoiInPoiDetailPage(PoiDetailRecRequest poiDetailRecRequest) throws TException {
        HotelRecRequest hotelRecRequest = transformRequest(poiDetailRecRequest);
        HotelRecResponse hotelRecResponse = hotelRecClient.recommend(hotelRecRequest);
        PoiDetailRecResponse poiDetailRecResponse = transformResponse(hotelRecResponse);
        return poiDetailRecResponse;
    }

    /**
     * 将详情页推荐的请求转化为rec的请求
     * @param poiDetailRecRequest
     * @return
     */
    private static HotelRecRequest transformRequest(PoiDetailRecRequest poiDetailRecRequest){
        HotelRecRequest hotelRecRequest = new HotelRecRequest();
        if (RecRequestType.REC_PREORDER_POI == poiDetailRecRequest.getRequestType()) {
            hotelRecRequest.setServiceType(RecServiceType.REC_PREORDER_POI);
        } else if (RecRequestType.REC_VACATION_POI == poiDetailRecRequest.getRequestType()){
            hotelRecRequest.setServiceType(RecServiceType.REC_VACATION_POI);
        } else {
            hotelRecRequest.setServiceType(RecServiceType.POI_DETAIL_REC);
        }

        PoiDetailRecExtraMsg msg = new PoiDetailRecExtraMsg();
        UserRecInfo userRecInfo = new UserRecInfo();
        hotelRecRequest.setPoiDetailRecMsg(msg);
        hotelRecRequest.setUserInfo(userRecInfo);

        hotelRecRequest.setExtraDataMap(new HashMap<String, String>());
        RecMapUtils.putALl(hotelRecRequest.getExtraDataMap(), poiDetailRecRequest.getExtraMap());

        //详情页poi
        List<Integer> poiOnShow = new ArrayList<Integer>();
        if (poiDetailRecRequest.isSetDetailPoiInfo()){
            DetailPoiInfo detailPoiInfo = poiDetailRecRequest.getDetailPoiInfo();
            poiOnShow.add(detailPoiInfo.getPoiId());
            RecMapUtils.putALl(hotelRecRequest.getExtraDataMap(), detailPoiInfo.getExtraMap());
        }
        hotelRecRequest.setPoiOnShow(poiOnShow);

        //user的信息
        if (poiDetailRecRequest.isSetDetailUserInfo()){
            DetailUserInfo detailUserInfo = poiDetailRecRequest.getDetailUserInfo();

            //uuid
            String uuid = detailUserInfo.getUuid();
            userRecInfo.setUuid(uuid);

            //userId
            long userId = detailUserInfo.getUserId();
            userRecInfo.setUserId(userId);

            //住宿类型
            AccommodationType accType = getAccType(detailUserInfo.getAccommodationType());
            userRecInfo.setAccType(accType);

            //user位置
            Location userLocation = new Location(detailUserInfo.getUserLat(), detailUserInfo.getUserLng());
            userRecInfo.setUserLocation(userLocation);

            //check-in/out date
            userRecInfo.setCheckInDate(detailUserInfo.isSetDateCheckIn()? detailUserInfo.getDateCheckIn() : Utils.getDayNum(0,null));
            userRecInfo.setCheckOutDate(detailUserInfo.isSetDateCheckOut()? detailUserInfo.getDateCheckOut() : Utils.getDayNum(0,null));

            //client type and version
            userRecInfo.setClientType(detailUserInfo.getClientType());
            userRecInfo.setAppVersion(detailUserInfo.getAppVersion());

            userRecInfo.setAppCityId(detailUserInfo.getUserCityId());
            userRecInfo.setChannelCityId(detailUserInfo.getChannelCityId());
            userRecInfo.setAppCityId(detailUserInfo.getAppCityId());

            //extra info
            userRecInfo.setExtraDataMap(new HashMap<String, String>());
            RecMapUtils.putALl(userRecInfo.getExtraDataMap(), detailUserInfo.getExtraMap());
            userRecInfo.putToExtraDataMap("actionTime", String.valueOf(detailUserInfo.getActionTime()));
        }

        hotelRecRequest.setStrategy(poiDetailRecRequest.getStrategy());

        return hotelRecRequest;
    }

    /**
     * 将rec的结果转化为详情页的结果
     * @param hotelRecResponse
     * @return
     */
    private static PoiDetailRecResponse transformResponse(HotelRecResponse hotelRecResponse){
        PoiDetailRecResponse poiDetailRecResponse = new PoiDetailRecResponse();

        RecResponseStatus status;
        RecServiceStatus recServiceStatus = hotelRecResponse.getServiceStatus();
        if (RecServiceStatus.DEGRADE == recServiceStatus){
            status = RecResponseStatus.DEGRADE;
        } else if (RecServiceStatus.ERROR == recServiceStatus){
            status = RecResponseStatus.ERROR;
        } else if (RecServiceStatus.FORBIDDEN == recServiceStatus){
            status = RecResponseStatus.FORBIDDEN;
        } else if (RecServiceStatus.UNCONNECT == recServiceStatus){
            status = RecResponseStatus.UNCONNECT;
        } else{
            status = RecResponseStatus.OK;
        }
        poiDetailRecResponse.setStatus(status);

        poiDetailRecResponse.setExtraMap(hotelRecResponse.getExtraDataMap());

        poiDetailRecResponse.setStrategy(hotelRecResponse.getStrategy());

        List<PoiDetailResp> poiDetailRespList = new ArrayList<PoiDetailResp>();
        if (hotelRecResponse.getPoiRecListSize() > 0){
            for (PoiRecInfo poiRecInfo: hotelRecResponse.getPoiRecList()){
                PoiDetailResp poiDetailResp = new PoiDetailResp();

                poiDetailResp.setPoiId(poiRecInfo.getPoiId());
                poiDetailResp.setStid(poiRecInfo.getCt_poi());
                poiDetailResp.setDistanceToPoi(poiRecInfo.getDistanceToRequest());
                poiDetailResp.setDistanceToUser(poiRecInfo.getDistanceToUser());
                poiDetailResp.setExtraMap(poiRecInfo.getExtraDataMap());

                poiDetailRespList.add(poiDetailResp);
            }
        }
        poiDetailRecResponse.setPoiRecList(poiDetailRespList);

        return poiDetailRecResponse;
    }

    /**
     * 转换住宿类型
     * @param accType
     * @return
     */
    private static AccommodationType getAccType(String accType){
        if ("HR".equalsIgnoreCase(accType)){
            return AccommodationType.HR;
        } else {
            return AccommodationType.DR;
        }
    }

}
