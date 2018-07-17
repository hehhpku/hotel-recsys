package com.meituan.hotel.rec.service.rawRank.implement;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.rawRank.IRawRanker;
import com.meituan.hotel.rec.service.rawRank.ScoreCalculator;
import com.meituan.hotel.rec.service.rawRank.common.Weight;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.*;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hehuihui on 3/22/16
 */
@Service("select-rec-raw-rank-service")
public class SelectRecRawRanker implements IRawRanker{

    public static final  Logger logger = RecUtils.getLogger(SelectRecRawRanker.class.getSimpleName());

    public static final String STRATEGY = SelectRecRawRanker.class.getSimpleName();

    @Override
    public RecResponse rawRank(RecRequest request, List<PoiEntry> poiEntryList, JSONObject joLog) {

        Weight weight = getWeight(request);
        double maxDistance = poiEntryList.get(poiEntryList.size() - 1).getDistanceToRequest();
        for (PoiEntry e: poiEntryList){
            double distanceS = ScoreCalculator.calDistanceScore(e.getDistanceToRequest(), maxDistance);
            double hotelTypeS = 0;
            double brandS = ScoreCalculator.calBrandScore(e.getBrandId(), request.getBrandId());
            double priceS = ScoreCalculator.calPriceScore(request.getPriceLow(), request.getPriceHigh(), e.getLowestPrice());
            double gradeS = ScoreCalculator.calSaleNumAndGradeScore(e.getAvgScore(), e.getMarkNumbers());
            double score = weight.getDistanceWeight() * distanceS
                    + weight.getHotelTypeWeight() * hotelTypeS
                    + weight.getBrandIDWeight() * brandS
                    + weight.getPriceWeight() * priceS
                    + weight.getGradeWeight() * gradeS;
            e.setRawRankScore(score);
        }

        Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
            @Override
            public int compare(PoiEntry o1, PoiEntry o2) {
                if (o1.getSource() == o2.getSource()){
                    return Double.valueOf(o1.getRawRankScore()).compareTo(o2.getRawRankScore());
                } else {
                    return RecUtils.comparePoiScore(o1, o2);
                }
            }
        }));

        RecResponse response = new RecResponse();
        response.setStrategy(STRATEGY);
        response.setPoiEntryList(poiEntryList);
        response.setServiceStatus(RecServiceStatus.OK);
        return response;
    }

    /**
     * 动态权重策略
     * @param request
     * @return
     */
    public static Weight getWeight(RecRequest request){
        Weight weight;
        if (AccommodationType.HR == request.getAccommodationType() ){
            weight = new Weight(5, 4, 2,   3.5, 2);
        } else {
            weight = new Weight(5, 3, 2.5, 3,   2);
        }
        updateWeightByRequest(request, weight);
        return weight;
    }

    /**
     * 根据筛选条件更新权重
     * @param request
     * @param weight
     * @return
     */
    private static void updateWeightByRequest(RecRequest request, Weight weight){
        //筛选价格
        if (request.getPriceLow() > 1 || request.getPriceHigh() < 1000)
            weight.setPriceWeight(Math.min(5,weight.getPriceWeight() + 0.5));
        //筛选酒店类型
        if (CollectionUtils.isNotEmpty(request.getHotelType()) && !request.getHotelType().contains(20))
            weight.setHotelTypeWeight(Math.min(5,weight.getHotelTypeWeight() + 1.0));
        //筛选酒店品牌
        if (CollectionUtils.isNotEmpty(request.getBrandId()))
            weight.setBrandIDWeight(Math.min(5,weight.getBrandIDWeight() + 1.0));
    }
}
