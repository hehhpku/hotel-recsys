package com.meituan.hotel.rec.service.rawRank.implement;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.rawRank.IRawRanker;
import com.meituan.hotel.rec.service.rawRank.ScoreCalculator;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.RecServiceStatus;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by jiangweisen, hehuihui on 3/22/16
 */
@Service("search-rec-raw-rank-service")
public class SearchRecRawRanker implements IRawRanker {

    public static final Logger logger = RecUtils.getLogger(SelectRecRawRanker.class.getSimpleName());

    public static final String STRATEGY = SearchRecRawRanker.class.getSimpleName();

    @Override
    public RecResponse rawRank(RecRequest request, List<PoiEntry> poiEntryList, JSONObject joLog) {

        for (PoiEntry e: poiEntryList){
            double saleS = ScoreCalculator.calcSaleScore(e.getSales());
            double distanceS = ScoreCalculator.calDistanceScore(e.getDistanceToRequest(), 8000, e.getSource(), saleS);
            if (distanceS < 1){
                saleS /= (2 - distanceS);
            }
            double priceS = 0;
            if (request.getSearchPoiPrice() > 0.001){
                priceS = ScoreCalculator.calPriceScore(e.getLowestPrice(), request.getSearchPoiPrice());
            }
            double totalS = 7 * distanceS + 4 * saleS + 2 * e.getAvgScore();
            if (priceS < 0.5){
                totalS *= priceS;
            }
            e.setRawRankScore(totalS);
        }

        Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
            @Override
            public int compare(PoiEntry o1, PoiEntry o2) {
                return Double.valueOf(o1.getRawRankScore()).compareTo(o2.getRawRankScore());
            }
        }));

        RecResponse response = new RecResponse();
        response.setStrategy(STRATEGY);
        response.setPoiEntryList(poiEntryList);
        response.setServiceStatus(RecServiceStatus.OK);

        return response;
    }
}
