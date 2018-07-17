package com.meituan.hotel.rec.cross.impl.travel;

import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.hotel.rec.cross.impl.Util.AvailField;
import com.meituan.hotel.rec.cross.impl.Util.TravelOrHotelPoiInfo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 根据poi的销量及结束时间过滤poi
 * Created by zuolin on 15/11/7.
 */
public class TravelPoiFilter {

    private static Logger logger = LoggerFactory.getLogger(TravelPoiFilter.class);
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 过滤poi
     * @param travelOrHotelPoiInfo
     * @param request
     * @return
     */
    public boolean filterPoi(TravelOrHotelPoiInfo travelOrHotelPoiInfo, CrossRecRequest request){
        if(filterTime(travelOrHotelPoiInfo) &&
                filterConsume(travelOrHotelPoiInfo) &&
                filterUnusedTravelPoiid(travelOrHotelPoiInfo,request)){
            return true;
        }
        return false;
    }

    /**
     * 根据时间过滤poi
     * @param travelOrHotelPoiInfo
     * @return
     */
    public boolean filterTime(TravelOrHotelPoiInfo travelOrHotelPoiInfo){
        String lastDealDate = travelOrHotelPoiInfo.getPoiBaseAttribute().getLastDealDate();
        Date endTime ;
        try{
            endTime = sdf.parse(lastDealDate + " 00:00:00");
        }catch (Exception e){
            logger.error(AvailField.DATE_PARSE_EXCEPTION,e);
            return false;
        }

        Date nowTime = new Date();
        long timeSub = endTime.getTime() - nowTime.getTime();

        if (timeSub < 0){
            return false;
        }else{
            return true;
        }
    }

    /**
     * 根据销量过滤poi,consume < 0,poi过滤掉
     * @param travelOrHotelPoiInfo
     * @return
     */
    public boolean filterConsume(TravelOrHotelPoiInfo travelOrHotelPoiInfo){
        int consume = 0;

        try{
            consume = travelOrHotelPoiInfo.getPoiBaseAttribute().getConsume();
        }catch (Exception e){
            logger.error(AvailField.CONSUME_GET_EXCEPTION,e);
            return false;
        }
        if (consume <= 0){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 根据用户未消费的旅游单过滤poi
     * @param travelOrHotelPoiInfo
     * @param request
     * @return
     */
    public boolean filterUnusedTravelPoiid(TravelOrHotelPoiInfo travelOrHotelPoiInfo, CrossRecRequest request){
        List<Long> unusedTravelPoiid = request.getUnusedTravelPoiid();
        if(CollectionUtils.isEmpty(unusedTravelPoiid)){
            return true;
        }
        for(long poiid : unusedTravelPoiid){
            if(travelOrHotelPoiInfo.getPoiId() == poiid){
                return false;
            }
        }
        return true;
    }

}
