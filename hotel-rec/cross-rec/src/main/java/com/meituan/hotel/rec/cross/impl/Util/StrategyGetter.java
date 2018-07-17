package com.meituan.hotel.rec.cross.impl.Util;

import com.meituan.hotel.data.common.strategy.StrategyFactory;
import com.meituan.hotel.rec.cross.CrossRecRequest;
import com.meituan.jmonitor.JMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by zuolin on 15/12/8.
 */
public class StrategyGetter {
    private static Logger logger = LoggerFactory.getLogger(StrategyGetter.class);
    @Autowired
    private StrategyFactory strategyFactory;

    public void setStrategyFactory(StrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    /**
     * 获取策略名称
     * @param request
     * @return
     */
    public String getStrategyName(CrossRecRequest request) {
        String uuid = request.getUuid();
        String node = "hbdata.service.recommend.crossRec";
//        String node = "sample.crossRec";
        String service = "recommend";
        String strategy = "default";
        try {
            strategy = strategyFactory.buildUuidStrategy(uuid, node, service);
        } catch (Exception e) {
            logger.error(AvailField.STRATEGY_READ_EXCEPTION, e);
            JMonitor.add(JMonitorKey.STRATEGY_NAME_EXCEPTION);
        }
        return strategy;
    }
}
