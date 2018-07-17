package com.meituan.hotel.rec.service.rerank.implement.search_rec;

import com.meituan.hotel.rec.service.rerank.IReranker;
import com.meituan.hotel.rec.service.rerank.feature.FeatureUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.mobile.recommend.GBDT;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by hehuihui on 4/5/16
 */
@Service("rerank-searchrec-Xgboost_20160328")
public class Xgboost_searchRec_20160328 extends IReranker {

    private static final Logger logger = RecUtils.getLogger(Xgboost_searchRec_20160328.class.getSimpleName());

    private static GBDT gbdt = new GBDT();
    private static Map<String, Integer> featureNameIndexMap;

    private final static String MODEL_FILE
            = RecUtils.getResourceFile("model/search_rec/xgboost_20160328/xgboost_20160328.model");
    private final static String FEATURE_INDEX_FILE
            = RecUtils.getResourceFile("model/search_rec/xgboost_20160328/gbdt_feature_v2.map");

    static {
        try {
            gbdt.loadFromFile(MODEL_FILE);
            logger.info(RecUtils.getInfoString("LoadSearchRecXgboost_20160328DONE"));
            featureNameIndexMap = FeatureUtils.loadFeatureIndex(FEATURE_INDEX_FILE);
        } catch (Exception e) {
            logger.error("[ERROR] Loading gbdt model(Xgboost_searchRec_20160328 )", e);
        }
    }

    @Override
    protected GBDT getModel() {
        return gbdt;
    }

    @Override
    protected Map<String, Integer> getFeatureNameIndexMap() {
        return featureNameIndexMap;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
