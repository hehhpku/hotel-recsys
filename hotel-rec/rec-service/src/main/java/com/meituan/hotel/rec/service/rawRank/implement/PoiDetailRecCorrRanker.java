package com.meituan.hotel.rec.service.rawRank.implement;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.rawRank.*;
import com.meituan.hotel.rec.thrift.RecServiceStatus;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by zuolin and hehuihui on 3/23/16
 */
@Service("poi-detail-rec-corr-ranker")
public class PoiDetailRecCorrRanker implements IRawRanker {
    public static final double MAX_DISTANCE = 5000;

    public static final String STRATEGY =  PoiDetailRecCorrRanker.class.getSimpleName();

    public RecResponse rawRank(RecRequest request, List<PoiEntry> poiEntryList, JSONObject joLog) {
        // 计算poi粗排得分
        for (PoiEntry e : poiEntryList) {
            double distanceS = ScoreCalculator.calDistanceScore2(e.getDistanceToRequest(), MAX_DISTANCE);
            double saleS = ScoreCalculator.calcSaleScore(e.getSales());
            double priceS = ScoreCalculator.calPriceScore(request.getPriceLow(), e.getLowestPrice());
            double markS = ScoreCalculator.calMarkScore(e.getAvgScore(), e.getMarkNumbers());
            double score =  e.getCorrScore() * 30 + distanceS * 35 + saleS * 4.5 + priceS * 18;
            e.setDistanceScore(distanceS);
            e.setSalesScore(saleS);
            e.setPriceScore(priceS);
            e.setMarkScore(markS);
            e.setRawRankScore(score);
        }

        // 降序
        Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
            @Override
            public int compare(PoiEntry p1, PoiEntry p2) {
                return Double.valueOf(p1.getRawRankScore()).compareTo(p2.getRawRankScore());
            }
        }));

        RecResponse recResponse = new RecResponse();
        recResponse.setPoiEntryList(poiEntryList);
        recResponse.setServiceStatus(RecServiceStatus.OK);
        recResponse.setStrategy(STRATEGY);

        return recResponse;
    }
}
