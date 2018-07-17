package com.meituan.hotel.rec.client.select;

import com.meituan.hotel.rec.client.HotelRecClient;
import com.meituan.hotel.rec.client.common.Utils;
import com.meituan.hotel.rec.select.*;
import com.meituan.hotel.rec.thrift.*;

import org.apache.thrift.TException;

import java.util.*;

/**
 * Author: jiangweisen,jiangweisen@meituan.com Date: 11/11/15
 */
public class SelectRecClient {

    private HotelRecClient hotelRecClient;

    @Deprecated
    public SelectRecResponse selectRecommend(SelectRecRequest selectRecRequest) throws TException {
        SelectRecResponse selectRecResponse;
        HotelRecRequest hotelRecRequest = transRecRequest(selectRecRequest);
        HotelRecResponse hotelRecResponse = hotelRecClient.recommend(hotelRecRequest);
        System.out.println(hotelRecResponse);
        selectRecResponse = transRecResponse(hotelRecResponse);
        return selectRecResponse;
    }

    /**
     * 将select-rec的req转化为rec的req
     * @param selectRecRequest
     * @return
     */
    private static HotelRecRequest transRecRequest(SelectRecRequest selectRecRequest){
        HotelRecRequest hotelRecRequest = new HotelRecRequest();
        hotelRecRequest.setServiceType(RecServiceType.SELECT_REC);

        UserRecInfo userRecInfo = new UserRecInfo();
        SelectRecExtraMsg selectRecExtraMsg = new SelectRecExtraMsg();
        hotelRecRequest.setUserInfo(userRecInfo);
        hotelRecRequest.setSelectRecMsg(selectRecExtraMsg);

        String uuid = selectRecRequest.getUuid();
        userRecInfo.setUuid(uuid);

        int dateCheckIn = Integer.parseInt(Utils.getDayString(0, null));
        int dateCheckOut = Integer.parseInt(Utils.getDayString(0, null));
        int appCityId = 0;
        List<Integer> poiOnShow = new ArrayList<Integer>();
        try {
            if (selectRecRequest.getExtraMap() != null){
                //传递map的信息
                Map<String, String> map = selectRecRequest.getExtraMap();
                for (Map.Entry<String, String> e: map.entrySet()){
                    hotelRecRequest.putToExtraDataMap(e.getKey(), e.getValue());
                }
                if (map.containsKey("ci")) {
                    appCityId = Integer.parseInt(map.get("ci"));
                }
                if (map.containsKey("selectedPoi")){
                    poiOnShow = Utils.getIntListFromString(map.get("selectedPoi"), ",");
                }
            }
            dateCheckIn = Integer.parseInt(selectRecRequest.getDateCheckin());
            dateCheckOut = Integer.parseInt(selectRecRequest.getDateChechout());
        } catch (Exception e){
            e.printStackTrace();
        }
        userRecInfo.setCheckInDate(dateCheckIn);
        userRecInfo.setCheckOutDate(dateCheckOut);
        userRecInfo.setAppCityId(appCityId);
        hotelRecRequest.setPoiOnShow(poiOnShow);

        String strategy = selectRecRequest.getStrategy();
        hotelRecRequest.setStrategy(strategy);

        List<Integer> roomTypeList = selectRecRequest.getRoomTypeList();
        selectRecExtraMsg.setRoomTypeList(roomTypeList);

        AccommodationType accType = getAccType(selectRecRequest.getAccommodationType());
        userRecInfo.setAccType(accType);

        List<Integer> hotelTypeList = selectRecRequest.getHotelType();
        selectRecExtraMsg.setHotelType(hotelTypeList);

        List<Integer> brandIdList = selectRecRequest.getBrandId();
        selectRecExtraMsg.setBrandId(brandIdList);

        Location requestLocation = new Location(selectRecRequest.getLat(), selectRecRequest.getLng());
        userRecInfo.setUserLocation(requestLocation);
        selectRecExtraMsg.setRequestLocation(requestLocation);

        double priceLow = selectRecRequest.isSetPriceLow()?
                selectRecRequest.getPriceLow(): 0.0;
        double priceHigh = selectRecRequest.isSetPriceHigh()?
                selectRecRequest.getPriceHigh(): Double.MAX_VALUE;
        selectRecExtraMsg.setPriceLow(priceLow);
        selectRecExtraMsg.setPriceHigh(priceHigh);

        String sortingMethod = selectRecRequest.getSortingMethod();
        hotelRecRequest.setSortingMethod(getSortingMethod(sortingMethod));

        String clientType = selectRecRequest.getClientType();
        userRecInfo.setClientType(clientType);

        String appVersion = selectRecRequest.getAppVersion();
        userRecInfo.setAppVersion(appVersion);

        int locationType = selectRecRequest.getLocationType();
        selectRecExtraMsg.setLocationType(locationType);

        int businessType = selectRecRequest.getBusinessType();
        selectRecExtraMsg.setBusinessType(businessType);

        int receiptProvided = selectRecRequest.getReceiptProvided();
        selectRecExtraMsg.setReceiptProvided(receiptProvided);

        int channelCityId = selectRecRequest.getCityId();
        userRecInfo.setChannelCityId(channelCityId);

        return hotelRecRequest;
    }

    /**
     * 将rec的结果转化为select-rec要求的结果
     * @param hotelRecResponse
     * @return
     */
    private static SelectRecResponse transRecResponse(HotelRecResponse hotelRecResponse){
        SelectRecResponse selectRecResponse = new SelectRecResponse();
        String strategy = hotelRecResponse.getStrategy();
        selectRecResponse.setStrategy(strategy);
        List<Integer> poiIdList = new ArrayList<Integer>();
        if (RecServiceStatus.OK == hotelRecResponse.getServiceStatus()){
            poiIdList = Utils.getPoiIdList(hotelRecResponse.getPoiRecList());
        }
        selectRecResponse.setPoiArrayList(poiIdList);
        selectRecResponse.setTotalNumOfPoi(poiIdList.size());
        return selectRecResponse;
    }

    /**
     * 将筛选的住宿类型转化格式
     * @param type
     * @return
     */
    public static AccommodationType getAccType(int type){
        if (type == 2){
            return AccommodationType.HR;
        } else if (type == 1){
            return AccommodationType.DR;
        } else {
            return AccommodationType.OTH;
        }
    }

    /**
     * 将筛选的价格类型转化格式
     * @param s
     * @return
     */
    public static SortingMethod getSortingMethod(String s){
        if ("smarts".equalsIgnoreCase(s)){
            return SortingMethod.SMART;
        } else if ("distance".equalsIgnoreCase(s)){
            return SortingMethod.DISTANCE;
        } else if ("sold".equalsIgnoreCase(s)){
            return SortingMethod.SOLD;
        } else if ("avgscore".equalsIgnoreCase(s)){
            return SortingMethod.AVG_SCORE;
        } else if ("lowestprice".equals(s)){
            return SortingMethod.LOWEST_PRICE;
        } else if ("highestprice".equalsIgnoreCase(s)){
            return SortingMethod.HIGH_PRICE;
        } else {
            return SortingMethod.SMART;
        }
    }

    public void setHotelRecClient(HotelRecClient hotelRecClient) {
        this.hotelRecClient = hotelRecClient;
    }
}
