package com.meituan.hotel.rec.service.rawRank;

import com.meituan.hotel.rec.service.common.*;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by hehuihui on 3/22/16
 */
public interface IRawRanker {
    RecResponse rawRank(RecRequest request, List<PoiEntry> poiEntryList, JSONObject joLog);
}
