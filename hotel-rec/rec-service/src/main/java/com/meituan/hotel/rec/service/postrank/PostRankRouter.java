package com.meituan.hotel.rec.service.postrank;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.utils.RecUtils;

import com.meituan.hotel.rec.thrift.RecServiceType;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.*;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 5/9/16
 */
public class PostRankRouter {
    private static final Logger logger = RecUtils.getLogger(PostRankRouter.class.getSimpleName());

    @Resource
    private DistanceBasedPostRanker defaultPostRanker;

    @Resource
    private HighStarHotelRule highStarHotelRule;

    private Map<String, AbstractPostRanker> postRankerMap;

    public List<PoiEntry> postRank(List<PoiEntry> poiEntryList, RecRequest request, JSONObject joLog){
        if (CollectionUtils.isEmpty(poiEntryList)){
            return poiEntryList;
        }

        AbstractPostRanker postRanker = defaultPostRanker;
        if (postRankerMap.containsKey(request.getStrategy())){
            postRanker = postRankerMap.get(request.getStrategy());
        }

        try{
            poiEntryList = postRanker.postRank(poiEntryList, request, joLog);
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("PostRankRouter()"), e);
        }

//        if (request.getServiceType() == RecServiceType.POI_DETAIL_REC) {
//            try {
//                poiEntryList = highStarHotelRule.rerankRule(request, poiEntryList);
//            } catch (Exception e) {
//                logger.error(RecUtils.getErrorString("highStarHotelRule()"), e);
//            }
//        }

        return poiEntryList;
    }

    public void setPostRankerMap(Map<String, AbstractPostRanker> postRankerMap) {
        this.postRankerMap = postRankerMap;
    }
}
