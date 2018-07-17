package com.meituan.hotel.rec.service.postrank;

import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.RecRequest;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by hehuihui on 5/9/16
 */
@Service("simple-post-rank-service")
public class SimplePostRanker extends AbstractPostRanker {
    @Override
    public List<PoiEntry> postRank(List<PoiEntry> poiEntryList, RecRequest request, JSONObject joLog) {
        return poiEntryList;
    }
}
