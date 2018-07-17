package com.meituan.hotel.rec.service.rerank.implement.select_rec;

import com.meituan.hotel.rec.service.rerank.IReranker;
import com.meituan.hotel.rec.service.rerank.feature.FeatureUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.mobile.recommend.GBDT;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Author: hehuihui@meituan.com Date: 2/3/16
 */
@Service("rerank_gbdt_v4_RTFeature")
public class GBDT_SelectRecV4 extends IReranker{
    private static final Logger logger = RecUtils.getLogger( GBDT_SelectRecV4.class.getSimpleName());

    private static GBDT gbdt = new GBDT();
    private static Map<String, Integer> featureNameIndexMap;

    private final static String MODEL_FILE
            = RecUtils.getResourceFile("model/select_rec/gbdt_v4/gbdt.model.1454644086");
    private final static String FEATURE_INDEX_FILE
            = RecUtils.getResourceFile("model/select_rec/gbdt_v4/feature_index_map_v4");

    static {
        try {
            gbdt.loadFromFile(MODEL_FILE);
            logger.info(RecUtils.getInfoString("LoadSelectRecV4ModelDONE"));
            featureNameIndexMap = FeatureUtils.loadFeatureIndex(FEATURE_INDEX_FILE);
        } catch (Exception e) {
            logger.error("[ERROR] Loading gbdt model(GBDT V4)",e);
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
