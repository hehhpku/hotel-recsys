package com.meituan.hotel.rec.service.postrank;

import com.meituan.hotel.rec.service.common.*;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by hehuihui on 5/9/16
 */
@Service("distance-based-post-rank-service")
public class DistanceBasedPostRanker extends AbstractPostRanker {
    @Override
    public List<PoiEntry> postRank(List<PoiEntry> poiEntryList, RecRequest request, JSONObject joLog) {
        if (CollectionUtils.isEmpty(poiEntryList)){
            return poiEntryList;
        }

        poiEntryList = sortWithRequest(poiEntryList, request.getSortingMethod());

        return poiEntryList;
    }


}
