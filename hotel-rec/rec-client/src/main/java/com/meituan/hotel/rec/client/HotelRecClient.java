package com.meituan.hotel.rec.client;

import com.meituan.hotel.rec.thrift.*;

import org.apache.thrift.TException;

/**
 * Created by jiangweisen on 3/30/16
 */
public class HotelRecClient {
    private HotelRecService.Iface hotelRecService;

    public void setHotelRecService(HotelRecService.Iface hotelRecService) {
        this.hotelRecService = hotelRecService;
    }

    public HotelRecResponse recommend(HotelRecRequest request) throws TException{
        return hotelRecService.recommend(request);
    }
}
