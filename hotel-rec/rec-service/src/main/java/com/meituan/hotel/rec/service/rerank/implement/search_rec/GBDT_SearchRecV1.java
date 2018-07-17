package com.meituan.hotel.rec.service.rerank.implement.search_rec;

import com.meituan.hotel.rec.service.rerank.IReranker;
import com.meituan.hotel.rec.service.rerank.feature.FeatureUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.mobile.recommend.GBDT;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Author: hehuihui@meituan.com Date: 2/3/16
 */
@Service("rerank-searchrec-gbdt_v1-service")
public class GBDT_SearchRecV1 extends IReranker{
    private static final Logger logger = RecUtils.getLogger( GBDT_SearchRecV1.class.getSimpleName());

    private static GBDT gbdt = new GBDT();
    private static Map<String, Integer> featureNameIndexMap;

    private final static String MODEL_FILE
            = RecUtils.getResourceFile("model/search_rec/gbdt_v1/gbdt.model.20160125");
    private final static String FEATURE_INDEX_FILE
            = RecUtils.getResourceFile("model/search_rec/gbdt_v1/gbdt_feature.map");

    static {
        try {
            gbdt.loadFromFile(MODEL_FILE);
            logger.info(RecUtils.getInfoString("LoadSearchRecV1ModelDONE"));
            featureNameIndexMap = FeatureUtils.loadFeatureIndex(FEATURE_INDEX_FILE);
        } catch (Exception e) {
            logger.error("[ERROR] Loading gbdt model(GBDT V4)", e);
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
