package com.meituan.hotel.rec.client.cross;


import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.CrossRecResponse;
import com.meituan.hotel.rec.cross.HotelCrossRecService;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Author: jiangweisen@meituan.com, liujingjun@meituan.com
 * Date: 20/7/15
 * TIme: 8:48 PM
 * ----------------------------------------------
 * Desc :
 */
public class CrossRecClient {

    private HotelCrossRecService.Iface hotelCrossRecService;

    private static final Logger logger = LoggerFactory.getLogger("hotel-cross-rec");

    public void setHotelCrossRecService(HotelCrossRecService.Iface hotelCrossRecService) {
        this.hotelCrossRecService = hotelCrossRecService;
    }

    public Map<String, CrossRecResponse> crossRecommend(CrossRecRequest request) throws TException
    {
        Map<String, CrossRecResponse> recResponse = null;

        if (request.getUserId() <= 0) {
            logger.error("跨品类交叉推荐,请求userid="+request.getUuid()+" ERROR!!");
            return recResponse;
        }

        try {
            recResponse = hotelCrossRecService.crossRecommend(request);
        } catch (Exception e) {
            logger.error("跨品类交叉推荐异常", e);
        }
        return recResponse;
    }
}
