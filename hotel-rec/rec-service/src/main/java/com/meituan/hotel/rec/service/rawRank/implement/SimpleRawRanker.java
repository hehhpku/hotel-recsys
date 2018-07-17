package com.meituan.hotel.rec.service.rawRank.implement;

import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.common.RecResponse;
import com.meituan.hotel.rec.service.rawRank.IRawRanker;
import com.meituan.hotel.rec.thrift.RecServiceStatus;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by hehuihui on 3/22/16
 */
@Service("simple-raw-rank-service")
public class SimpleRawRanker implements IRawRanker {
    public static final String STRATEGY = SimpleRawRanker.class.getSimpleName();

    @Override
    public RecResponse rawRank(RecRequest request, List<PoiEntry> poiEntryList, JSONObject joLog) {
        RecResponse response = new RecResponse();
        response.setServiceStatus(RecServiceStatus.OK);
        response.setPoiEntryList(poiEntryList);
        response.setStrategy(STRATEGY);
        return response;
    }
}
