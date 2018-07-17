package com.meituan.hotel.rec.service.rawRank.implement;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.rawRank.*;
import com.meituan.hotel.rec.thrift.RecServiceStatus;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jiangweisen and hehuihui on 3/23/16
 */
@Service("poi-detail-rec-based-ranker")
public class PoiDetailRecBasedRanker implements IRawRanker{

    // 酒店的最远距离
    public static final double MAX_DISTANCE = 5000;

    public static final String STRATEGY = PoiDetailRecBasedRanker.class.getSimpleName();;

    public RecResponse rawRank(RecRequest request, List<PoiEntry> poiEntryList, JSONObject joLog) {

        for (PoiEntry e: poiEntryList){
            double distanceS = ScoreCalculator.calDistanceScore(e.getDistanceToRequest(), MAX_DISTANCE);
            double saleS = ScoreCalculator.calcSaleScore(e.getSales());
            double priceS = ScoreCalculator.calPriceScore(request.getPriceLow(), e.getLowestPrice());
            double markS = ScoreCalculator.calMarkScore(e.getAvgScore(), e.getMarkNumbers());
            if (saleS < 2.0){
                distanceS /= (3.0 - saleS);
            }
            double rawRankScore = (distanceS * 50 + saleS * 5 + markS) * priceS;
            e.setRawRankScore(rawRankScore);
        }

        Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
            @Override
            public int compare(PoiEntry o1, PoiEntry o2) {
                return Double.valueOf(o1.getRawRankScore()).compareTo(o2.getRawRankScore());
            }
        }));

        RecResponse response = new RecResponse();
        response.setPoiEntryList(poiEntryList);
        response.setStrategy(STRATEGY);
        response.setServiceStatus(RecServiceStatus.OK);

        return response;
    }
}
