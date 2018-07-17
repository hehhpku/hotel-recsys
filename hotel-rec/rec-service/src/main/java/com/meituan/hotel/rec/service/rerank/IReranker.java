package com.meituan.hotel.rec.service.rerank;

import com.meituan.hotel.rec.service.rerank.common.*;
import com.meituan.hotel.rec.service.rerank.feature.FeatureAssembler;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.mobile.recommend.GBDT;
import com.meituan.mobile.recommend.Tuple;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 3/25/16
 */
public abstract class IReranker {

    @Resource
    private FeatureAssembler featureAssembler;

    public RerankResponse rerank(RerankRequest request) {
        RerankResponse response = new RerankResponse();
        if (request == null || MapUtils.isEmpty(request.getPoiIdsFeatureMap())){
            response.setServiceStatus( RecServiceStatus.ERROR);
            return response;
        }

        GBDT gbdt = getModel();
        Map<String, Integer> featureNameIndexMap = getFeatureNameIndexMap();
        Logger logger = getLogger();

        try {
            Tuple featureTuple = new Tuple();
            Map<Integer, List<Double>> poiIdsFeatureMap = featureAssembler.assemblePoiIdsFeatureMap(request, featureNameIndexMap);
            Map<Integer, Double> poiIdsPredictScoreMap = new HashMap<Integer, Double>();
            for (Integer poiId : poiIdsFeatureMap.keySet()) {
                featureTuple.feature = poiIdsFeatureMap.get(poiId);
                double scorePredict = gbdt.predict(featureTuple);
                poiIdsPredictScoreMap.put(poiId, scorePredict);
            }
            response.setPoiidPredictScoreMap(poiIdsPredictScoreMap);
        } catch (Exception e) {
            logger.error(RecUtils.getErrorString("rerank()"), e);
            response.setServiceStatus(RecServiceStatus.ERROR);
            return response;
        }
        response.setServiceStatus(RecServiceStatus.OK);
        return response;
    }

    protected abstract GBDT getModel();

    protected abstract Map<String, Integer> getFeatureNameIndexMap();

    protected abstract Logger getLogger();


}
