package com.meituan.hotel.rec.service.rerank.implement.poidetail_rec;

import com.meituan.hotel.rec.service.rerank.IReranker;
import com.meituan.hotel.rec.service.rerank.common.RerankRequest;
import com.meituan.hotel.rec.service.rerank.common.RerankResponse;
import com.meituan.hotel.rec.service.rerank.feature.FeatureAssembler;
import com.meituan.hotel.rec.service.rerank.feature.FeatureUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.hotel.recommend.Tuple;
import com.meituan.hotel.recommend.Xgboost;
import com.meituan.mobile.recommend.GBDT;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hehuihui, on 5/10/16
 * Modified by zuolin02, on 5/28/16
 */
@Service("rerank-poidetailrec-xgboost-v3-service")
public class Poidetailrec_XGBoostRerank_V3 extends IReranker {

    @Resource
    private FeatureAssembler featureAssembler;

    private static Xgboost xgboost = new Xgboost(0.5);
    private static final Logger logger = RecUtils.getLogger(Poidetailrec_XGBoostRerank_V3.class.getSimpleName());
    private static Map<String, Integer> feature2IdxMap = new HashMap<String, Integer>();

    public static final String MODEL_FILE = "model/poidetail_rec/xgboost/xgboost_0607_pairwise";
    public static final String FEATURE_INDEX_FILE = RecUtils.getResourceFile("model/poidetail_rec/xgboost/feature_index_map_v2");

    static {
        try {
            xgboost.loadModel(MODEL_FILE);
            feature2IdxMap = FeatureUtils.loadFeatureIndex(FEATURE_INDEX_FILE);
            logger.info(RecUtils.getInfoString("load_poidetail_XGboost_Pairwise_Model_Done"));
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("load_poidetail_XGboost_Pairwise_Model"), e);
        }
    }

    @Override
    public RerankResponse rerank(RerankRequest request) {
        RerankResponse response = new RerankResponse();
        if (request == null || MapUtils.isEmpty(request.getPoiIdsFeatureMap())){
            response.setServiceStatus( RecServiceStatus.ERROR);
            return response;
        }

        Map<String, Integer> featureNameIndexMap = getFeatureNameIndexMap();
        Logger logger = getLogger();

        try {
            Map<Integer, List<Double>> poiIdsFeatureMap = featureAssembler.assemblePoiIdsFeatureMap(request, featureNameIndexMap);
            Map<Integer, Double> poiIdsPredictScoreMap = new HashMap<Integer, Double>();
            Tuple tuple = new Tuple();
            for (Integer poiId : poiIdsFeatureMap.keySet()) {
                tuple.feature = new HashMap<Integer, Double>();
                List<Double> featureList = poiIdsFeatureMap.get(poiId);
                int index = 0;
                StringBuffer sb = new StringBuffer();
                sb.append(1 + " ");
                for (double fea : featureList){
                    fea = transformFeature(fea);
                    if (isValidFeature(fea)){
                        tuple.feature.put(index, fea);
                        sb.append(index + ":" + fea + " ");
                        index += 1;
                    }
                }
                logger.debug(sb.toString());
                double scorePredict = xgboost.predict(tuple, false);
                logger.debug("this score = " + scorePredict);
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

    /**
     * @param feature
     * @return
     */
    public double transformFeature(double feature){
        return Double.parseDouble(String.format("%.6f", feature));
    }

    public boolean isValidFeature(double feature){
        if (Math.abs(feature - (-Math.pow(10, 10))) < 0.0000001){
            return false;
        }
        return true;
    }
    @Override
    protected GBDT getModel() {
        return null;
    }

    @Override
    protected Map<String, Integer> getFeatureNameIndexMap() {
        return feature2IdxMap;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

}
