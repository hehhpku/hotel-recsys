package com.meituan.hotel.rec.service.rerank.implement;

import com.meituan.hotel.rec.service.rerank.IReranker;
import com.meituan.hotel.rec.service.rerank.common.RerankRequest;
import com.meituan.hotel.rec.service.rerank.common.RerankResponse;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.mobile.recommend.GBDT;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by hehuihui on 3/25/16
 * 不进行rerank
 */
@Service("simple-rerank-service")
public class SimpleReranker extends IReranker {

    public static final String STRATEGY = "simpleRerankerStrategy";

    @Override
    public RerankResponse rerank(RerankRequest request) {
        RerankResponse response = new RerankResponse();
        response.setServiceStatus(RecServiceStatus.ERROR);
        return response;
    }

    @Override
    protected GBDT getModel() {
        return null;
    }

    @Override
    protected Map<String, Integer> getFeatureNameIndexMap() {
        return null;
    }

    @Override
    protected Logger getLogger() {
        return null;
    }
}
