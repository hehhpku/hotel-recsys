package com.meituan.hotel.rec.service.external;

import com.meituan.hotel.rec.data.*;
import com.meituan.hotel.rec.data.client.RecDataClient;
import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.Location;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hehuihui on 16/6/14.
 */
@Service
public class HotelStarService {
    private static Logger logger = RecUtils.getLogger(HotelStarService.class.getSimpleName());

    @Autowired
    private RecDataClient recDataClient;

    /**
     * 获取poi的基本信息，含有酒店星级数据
     * @param poiIds
     * @return
     */
    public Map<Integer, PoiEntry> getHotelPoiInfo(List<Integer> poiIds){
        RecDataRequest request = new RecDataRequest(RequestOption.GET_HOTEL_POI);
        PoiInfoReq poiInfoReq = new PoiInfoReq(poiIds);
        request.setPoiInfoReq(poiInfoReq);

        Map<Integer, PoiEntry> map = new HashMap<Integer, PoiEntry>();
        try {
            RecDataResponse response = recDataClient.getHotelPoiInfo(request);
            if (response.getStatus() == ResponseStatus.OK) {
                for (HotelPoiBasicInfo info : response.getPoiInfoList()){
                    map.put(info.getPoiId(), new PoiEntry(info));
                }
            }
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("getHotelPoiInfo"), e);
        }

        for (int poiId: poiIds){
            if (!map.containsKey(poiId)){
                map.put(poiId, new PoiEntry(poiId));
            }
        }
        return map;
    }

    /**
     * 根据城市，经纬度，星级获取高星酒店
     * @param stars
     * @param cities
     * @param location
     * @return
     */
    public List<PoiEntry> getHighStarPoi(List<Integer> stars, List<Integer> cities, Location location){
        RecDataRequest request = new RecDataRequest(RequestOption.GET_HIGHSTAR_POI);
        HighStarReq highStarReq = new HighStarReq(cities);highStarReq.setStarLists(stars);
        if (RecDistanceUtils.isValidLocation(location)){
            highStarReq.setLatitude(location.getLattitude());
            highStarReq.setLongitude(location.getLongitude());
        }
        request.setHighStarReq(highStarReq);

        List<PoiEntry> poiEntries = new ArrayList<PoiEntry>();
        try {
            RecDataResponse response = recDataClient.getHighStarHotel(request);
            if (response.getStatus() == ResponseStatus.OK){
                for (HotelPoiBasicInfo info : response.getPoiInfoList()){
                    poiEntries.add(new PoiEntry(info));
                }
            }
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("getHighStarPoi()"), e);
        }
        return poiEntries;
    }

}
