package com.meituan.hotel.rec.service.external;

import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.sinai.client.PoiClient;
import com.meituan.service.mobile.sinai.client.model.PoiModel;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: hehuihui@meituan.com Date: 2/25/16
 */
@Service("sinai-poi-client")
public class SinaiPoiClient {

    private static final Logger logger = RecUtils.getLogger(SinaiPoiClient.class.getSimpleName());

    @Autowired
    private PoiClient poiClient;

    /**
     * 多线程获取poi数据
     * @param poiList
     * @param fieldList
     * @param numPoiPerThread
     * @return
     */
    public List<PoiModel> multiThreadGetPoiModel(List<Integer> poiList, List<String> fieldList, int numPoiPerThread){
        List<PoiModel> poiModelList = new ArrayList<PoiModel>();
        if (CollectionUtils.isEmpty(poiList) || CollectionUtils.isEmpty(fieldList)){
            return poiModelList;
        }
        //个数小于100，单线程获取
        if (poiList.size() < 100){
            return getPoiModelFromSinai(poiList, fieldList);
        }
        //个数大于100，多线程从sinai查poi属性
        int begin = 0;
        int end = Math.min(poiList.size(),begin + numPoiPerThread);
        List<PoiGetterThread> poiGetThreadList = new ArrayList<PoiGetterThread>();
        for (begin = 0; begin < poiList.size(); begin = end, end = Math.min(begin + numPoiPerThread, poiList.size())){
            PoiGetterThread poiGetterThread = new PoiGetterThread(poiList.subList(begin, end), fieldList);
            poiGetThreadList.add(poiGetterThread);
        }
        for (PoiGetterThread thread: poiGetThreadList){
            thread.start();
        }
        for (PoiGetterThread thread: poiGetThreadList){
            try{
                thread.join(100);
            } catch (Exception e){
                logger.error(RecUtils.getErrorString("multiThreadGetPoiModel()"), e);
            }
        }
        for (PoiGetterThread thread: poiGetThreadList){
            poiModelList.addAll(thread.getPoiModelList());
        }
        return poiModelList;
    }

    /**
     * 从sinai获取数据
     * @param poiIdList
     * @param fieldList
     * @return
     */
    public List<PoiModel> getPoiModelFromSinai(List<Integer> poiIdList, List<String> fieldList){
        long start = System.currentTimeMillis();
        List<PoiModel> poiModelList = new ArrayList<PoiModel>();
        try {
            poiModelList = poiClient.listPois(poiIdList, fieldList);
        } catch (Exception e){
            logger.error(RecUtils.getErrorString( "getPoiModelFromSinai()") ,e);
        }
        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.POI_SINAI_TIME, timeCost);
        return poiModelList;
    }

    class PoiGetterThread extends Thread{
        private List<Integer> poiList;
        private List<PoiModel> poiModelList;
        private List<String> fieldList;

        public List<PoiModel> getPoiModelList() {
            return poiModelList;
        }

        public List<Integer> getPoiList() {
            return poiList;
        }

        public PoiGetterThread(List<Integer> poiList, List<String> fieldList){
            this.poiList = poiList;
            this.fieldList = fieldList;
        }
        @Override
        public void run() {
            poiModelList = getPoiModelFromSinai(poiList,fieldList);
        }
    }
}
