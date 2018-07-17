package com.meituan.hotel.rec.service.recall.implement;

import com.meituan.hotel.rec.data.client.RecDataClient;
import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.RecResponse;
import com.meituan.hotel.rec.service.constants.Constants;
import com.meituan.hotel.rec.service.external.HotelStarService;
import com.meituan.hotel.rec.service.recall.IRecaller;
import com.meituan.hotel.rec.service.recall.common.RecallRequest;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.hotel.rec.thrift.RecServiceType;
import com.meituan.service.mobile.prometheus.common.util.CollectionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by hehuihui on 16/6/14.
 * 高星召回策略
 */
@Service("high-star-poi-recall-service")
public class HighStarPoiRecaller implements IRecaller{
    public static final String STRATEGY = "highStarPoiRecallerStrategy";

    public static final int RECALL_NUM = 200;

    @Resource
    private HotelStarService hotelStarService;

    public static final List<Integer> DEFAULT_HIGH_STAR = Arrays.asList(0,1,2,3,4,5);

    @Override
    public RecResponse recall(RecallRequest request, JSONObject joLog) {
        RecResponse response = new RecResponse();

        List<Integer> stars = getStarLevel(request);
        // 非高星请求，直接返回
        if (CollectionUtils.isEmpty(stars)){
            response.setServiceStatus(RecServiceStatus.DEGRADE);
            return response;
        }

        // 高星请求，召回高星酒店
        List<PoiEntry> poiEntries = hotelStarService.getHighStarPoi(stars,
                request.getPoiCityIdList(), request.getRequestLocation());

        poiEntries = CollectionUtil.subList(poiEntries, 0, RECALL_NUM);
        response.setPoiEntryList(poiEntries);
        response.setServiceStatus(RecServiceStatus.OK);
        response.setStrategy(STRATEGY);

        return response;
    }

    /**
     * 根据请求获取高星星级
     * @param request
     * @return
     */
    public List<Integer> getStarLevel(RecallRequest request){
        List<Integer> stars = new ArrayList<Integer>();
        if (request.getType() == RecServiceType.POI_DETAIL_REC){
            int star = Constants.DEFAULT_HOTEL_STAR;
            if (CollectionUtils.isNotEmpty(request.getPoiOnShow())){
                int poiId = request.getPoiOnShow().get(0);
                Map<Integer, PoiEntry> map = hotelStarService.getHotelPoiInfo(Arrays.asList(poiId));
                if (map != null && map.containsKey(poiId)){
                    star = map.get(poiId).getHotelStar();
                }
            }
            // 加入高星的星级
            if (star < Constants.DEFAULT_HOTEL_STAR && star >= 0){
                stars.addAll(DEFAULT_HIGH_STAR);
            }

        }

        return stars;
    }
}
