package com.meituan.hotel.rec.service.rerank.implement.poidetail_rec;

import com.meituan.hotel.rec.service.rerank.IReranker;
import com.meituan.hotel.rec.service.rerank.feature.FeatureUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.mobile.recommend.GBDT;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hehuihui on 5/10/16
 */
@Service("rerank-poidetailrec-xgboost-v1-service")
public class Poidetailrec_XGBoostRerank_V1 extends IReranker {

    private static final Logger logger = RecUtils.getLogger(Poidetailrec_XGBoostRerank_V1.class.getSimpleName());

    private static GBDT gbdt = new GBDT();
    private static Map<String, Integer> feature2IdxMap = new HashMap<String, Integer>();

    public static final String MODEL_FILE = RecUtils.getResourceFile("model/poidetail_rec/xgboost/xgboost_0509");
    public static final String FEATURE_INDEX_FILE = RecUtils.getResourceFile("model/poidetail_rec/xgboost/feature_index_map");

    static {
        try {
            gbdt.loadFromFile(MODEL_FILE);
            feature2IdxMap = FeatureUtils.loadFeatureIndex(FEATURE_INDEX_FILE);
            logger.info(RecUtils.getInfoString("load_poidetail_XGboost_V1_Model_Done"));
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("load_poidetail_XGboost_V1_Model"), e);
        }
    }

    @Override
    protected GBDT getModel() {
        return gbdt;
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
