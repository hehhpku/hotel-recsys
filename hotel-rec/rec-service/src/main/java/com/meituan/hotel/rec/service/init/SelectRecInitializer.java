package com.meituan.hotel.rec.service.init;

import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.thrift.HotelRecRequest;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.hotel.rec.thrift.SelectRecExtraMsg;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hehuihui on 4/13/16
 */
@Service("select-rec-initializer")
public class SelectRecInitializer extends AbstractInitializer {
    @Override
    public void addInfo2Req(RecRequest request, HotelRecRequest hotelRecRequest, JSONObject joLog) {
        SelectRecExtraMsg selectRecMsg = hotelRecRequest.getSelectRecMsg();
        if (selectRecMsg != null){
            List<Integer> roomTypeList = selectRecMsg.getRoomTypeListSize() > 0?
                    selectRecMsg.getRoomTypeList(): Arrays.asList(0);
            List<Integer> hotelTypeList = selectRecMsg.getHotelTypeSize() > 0?
                    selectRecMsg.getHotelType(): Arrays.asList(20);
            List<Integer> brandIdList = selectRecMsg.getBrandIdSize() > 0?
                    selectRecMsg.getBrandId(): new ArrayList<Integer>();
            double priceLow = selectRecMsg.isSetPriceLow()?
                    selectRecMsg.getPriceLow(): Double.MIN_VALUE;
            double priceHigh = selectRecMsg.isSetPriceHigh()?
                    selectRecMsg.getPriceHigh(): Double.MAX_VALUE;
            Location requestLocation = RecDistanceUtils.isValidLocation(selectRecMsg.getRequestLocation())?
                    selectRecMsg.getRequestLocation(): RecDistanceUtils.getNullLocation();

            request.setRoomTypeList(roomTypeList);
            request.setHotelType(hotelTypeList);
            request.setBrandId(brandIdList);
            request.setPriceLow(priceLow);
            request.setPriceHigh(priceHigh);
            request.setRequestLocation(requestLocation);
        }
    }
}
