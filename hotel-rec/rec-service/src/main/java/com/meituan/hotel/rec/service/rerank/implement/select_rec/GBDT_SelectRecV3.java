package com.meituan.hotel.rec.service.rerank.implement.select_rec;

import com.meituan.hotel.rec.service.rerank.IReranker;
import com.meituan.hotel.rec.service.rerank.feature.FeatureUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.mobile.recommend.GBDT;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Author: hehuihui@meituan.com Date: 1/21/16
 */

@Service("rerank-selectrec-gbdt_v3-service")
public class GBDT_SelectRecV3 extends IReranker{

    private static final Logger logger = RecUtils.getLogger(GBDT_SelectRecV4.class.getSimpleName());

    private static GBDT gbdt = new GBDT();
    private static Map<String, Integer> featureNameIndexMap;

    private final static String MODEL_FILE
            = RecUtils.getResourceFile("model/select_rec/gbdt_v3/gbdt.model.1453304244");
    private final static String FEATURE_INDEX_FILE
            = RecUtils.getResourceFile("model/select_rec/gbdt_v3/feature_index_v3");

    static {
        try {
            gbdt.loadFromFile(MODEL_FILE);
            logger.info(RecUtils.getInfoString("LoadSelectRecV2ModelDONE"));
            featureNameIndexMap = FeatureUtils.loadFeatureIndex(FEATURE_INDEX_FILE);
        } catch (Exception e) {
            logger.error("[ERROR] Loading gbdt model(GBDT V3)",e);
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
