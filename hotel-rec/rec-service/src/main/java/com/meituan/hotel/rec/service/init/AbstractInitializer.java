package com.meituan.hotel.rec.service.init;

import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.external.DataHubRecClient;
import com.meituan.hotel.rec.service.external.RecStrategyGetter;
import com.meituan.hotel.rec.service.utils.RecDateUtils;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.thrift.*;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 4/13/16
 */
public abstract class AbstractInitializer {

    @Resource(name = "datahub-external-client")
    private DataHubRecClient dataHubRecClient;

    @Resource(name = "rec-strategy-getter-service")
    private RecStrategyGetter strategyGetter;

    public RecRequest initRequest(HotelRecRequest hotelRecRequest, JSONObject joLog){
        RecRequest request = new RecRequest();

        RecServiceType serviceType = hotelRecRequest.getServiceType();
        request.setServiceType(serviceType);
        //已展示结果
        List<Integer> poiOnShow = hotelRecRequest.getPoiOnShowSize() > 0?
                hotelRecRequest.getPoiOnShow(): new ArrayList<Integer>();
        request.setPoiOnShowList(poiOnShow);

        request.setSortingMethod(
                hotelRecRequest.isSetSortingMethod() ? hotelRecRequest.getSortingMethod() : SortingMethod.SMART);

        //用户信息
        UserRecInfo userInfo = hotelRecRequest.isSetUserInfo()?
                hotelRecRequest.getUserInfo(): new UserRecInfo("uuid_default");

        request.setAccommodationType(
                userInfo.isSetAccType() ? userInfo.getAccType() : AccommodationType.DR);

        int channelCityId = userInfo.getChannelCityId();
        request.setChannelCityId(channelCityId);

        String uuid = userInfo.getUuid();
        request.setUuid(uuid);

        long userId = userInfo.getUserId();
        if (userId <= 0){
            userId = dataHubRecClient.getUserIdFromUuid(uuid);
        }
        request.setUserId(userId);

        String strategy = hotelRecRequest.getStrategy();
        if (StringUtils.isBlank(strategy)){
            strategy = strategyGetter.getStrategy(uuid, serviceType, userInfo.getClientType());
        }
        request.setStrategy(strategy);

        Location userLocation = RecDistanceUtils.isValidLocation(userInfo.getUserLocation())?
                userInfo.getUserLocation(): RecDistanceUtils.getNullLocation();
        request.setUserLocation(userLocation);

        request.setCheckInDate(
                userInfo.isSetCheckInDate() ? userInfo.getCheckInDate() : RecDateUtils.getDayNumFormat(0));
        request.setCheckOutDate(
                userInfo.isSetCheckOutDate() ? userInfo.getCheckOutDate() : RecDateUtils.getDayNumFormat(0));
        request.setAppCityId(userInfo.isSetAppCityId() ?
                userInfo.getAppCityId() : 0);
        request.setClientType(
                userInfo.isSetClientType() ? userInfo.getClientType() : "client_type");
        request.setAppVersion(
                userInfo.isSetAppVersion() ? userInfo.getAppVersion() : "0.0");

        // 默认设置用户请求的位置为定位位置
        // 在addInfo2Req接口里会由各个具体推荐服务修改这个请求位置
        request.setRequestLocation(userLocation);

        addInfo2Req(request, hotelRecRequest, joLog);
        return request;
    }

    public abstract void addInfo2Req(RecRequest request, HotelRecRequest hotelRecRequest, JSONObject joLog);
}
