package com.meituan.hotel.rec.service.external;

import com.meituan.hotel.data.datahub.bean.DimensionInfoBean;
import com.meituan.hotel.data.datahub.bean.FeatureInfoBean;
import com.meituan.hotel.data.datahub.client.DatahubFeatureClient;
import com.meituan.hotel.data.datahub.common.DatahubResponse;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.jmonitor.JMonitor;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Author: hehuihui@meituan.com Date: 2/24/16
 */
@Service("datahub-external-client")
public class DataHubRecClient {

    private static Logger logger = RecUtils.getLogger(DataHubRecClient.class.getSimpleName());
    @Autowired
    private DatahubFeatureClient datahubFeatureClient;

    public long getUserIdFromUuid(String uuid){
        long start = System.currentTimeMillis();
        long userId = -1;

        if (uuid == null)
            return userId;

        //准备参数
        final String domain = "htl-ruul";

        DimensionInfoBean dimBean = new DimensionInfoBean();
        dimBean.setIndex(1);
        dimBean.setName("uuid");
        Set<String> uuidSet = new HashSet<String>();
        uuidSet.add(uuid);
        dimBean.setValues(uuidSet);

        //从datahub获取userId, timeout = 100ms
        try {
            DatahubThread datahubThread = new DatahubThread(domain, dimBean, null);
            datahubThread.start();
            datahubThread.join(20);
            userId = datahubThread.getUserId(uuid);

        } catch (Exception e){
            logger.error(RecUtils.getErrorString("DataHubRecClient"), e);
        }

        long timeCost = System.currentTimeMillis() - start;
        logger.info(RecUtils.getTimeCostString("getUserIdFromUuid", timeCost));
        JMonitor.add(JmonitorKey.GET_USERID_FROM_UUID_TIME, timeCost);
        return userId;
    }

    class DatahubThread extends Thread{
        private String domain;
        private DimensionInfoBean dimBean;
        private FeatureInfoBean featureBean;

        DatahubResponse<Map<String, Map<String, String>>> datahubResponse = new DatahubResponse<Map<String, Map<String, String>>>();
        private long userId = -1;

        public DatahubThread( String domain, DimensionInfoBean dimBean,FeatureInfoBean featureBean) {
            this.domain = domain;
            this.dimBean = dimBean;
            this.featureBean = featureBean;
        }

        public long getUserId(String uuid){
            if (datahubResponse.isStatus()){
                try {
                    Map<String, Map<String, String>> dataMap = datahubResponse.getData();
                    if (MapUtils.isNotEmpty(dataMap) && dataMap.containsKey(uuid) && dataMap.get(uuid).containsKey("userid")){
                        userId = Long.parseLong(dataMap.get(uuid).get("userid"));
                    }
                }catch (Exception e){
                    logger.error(RecUtils.getWarnString("parseUserIdError"), e);
                }
            }
            return userId;
        }

        @Override
        public void run() {
            try {
                datahubResponse = datahubFeatureClient.getSingleDimFeatures(domain, dimBean, featureBean);
            } catch (Exception e){
                logger.warn(RecUtils.getWarnString("datahubException"), e);
                JMonitor.add(JmonitorKey.GET_USERID_FROM_UUID_EXP);
            }
        }
    }
}
