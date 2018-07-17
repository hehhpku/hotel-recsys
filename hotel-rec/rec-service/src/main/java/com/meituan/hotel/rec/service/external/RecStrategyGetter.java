package com.meituan.hotel.rec.service.external;


import com.meituan.hotel.data.common.strategy.StrategyFactory;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.RecServiceType;
import com.meituan.jmonitor.JMonitor;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by hehuihui on 3/16/16
 */
@Service("rec-strategy-getter-service")
public class RecStrategyGetter {
    Logger logger = RecUtils.getLogger(RecStrategyGetter.class.getName());
    @Autowired
    private StrategyFactory strategyFactory;

    /**
     * 从config获取策略
     * @param uuid
     * @param serviceType
     * @return
     */
    public String getStrategy(String uuid, RecServiceType serviceType, String clientType){
        long start = System.currentTimeMillis();
        String strategy = "simple-strategy";
        try {
            final String node = getNode(serviceType, clientType);
            final String serviceName = "recommend";
            if (node != null) {
                strategy = strategyFactory.buildUuidStrategy(uuid, node, serviceName);
            }
        } catch (Exception e) {
            logger.error(RecUtils.getErrorString("getStrategy()"), e);
            JMonitor.add(JmonitorKey.GET_STRATEGY_EXP);
        }
        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.GET_STRATEGY_TIME, timeCost);
        return strategy;
    }

    private String getNode(RecServiceType serviceType, String clientType){
        if (serviceType == RecServiceType.SELECT_REC) {
            if ("iphone".equalsIgnoreCase(clientType)) {
                return "hbdata.service.recommend.selectRec.iphone";
            } else {
                return "hbdata.service.recommend.selectRec.android";
            }
        }
        else  if (serviceType == RecServiceType.SEARCH_REC)
            return "hbdata.service.recommend.searchRec.rerank";
        else if (serviceType == RecServiceType.POI_DETAIL_REC)
            return "hbdata.service.recommend.poidetailRec";
        else if (serviceType == RecServiceType.CROSS_REC)
            return "hbdata.service.recommend.crossRec";
        else if (serviceType == RecServiceType.REC_VACATION_POI)
            return "hbdata.service.recommend.vacationRec";
        else
            return null;
    }
}
