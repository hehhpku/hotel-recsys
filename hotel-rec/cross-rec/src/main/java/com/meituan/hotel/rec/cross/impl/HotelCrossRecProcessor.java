
package com.meituan.hotel.rec.cross.impl;

import com.meituan.hotel.rec.cross.*;
import com.meituan.hotel.rec.cross.impl.Util.*;
import com.meituan.hotel.rec.cross.impl.scene.*;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.mtthrift.server.MTIface;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class HotelCrossRecProcessor extends MTIface implements HotelCrossRecService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(HotelCrossRecProcessor.class);
    private GenScene genScene = new GenScene();
    private SceneClass sceneClass ;

    @Autowired
    private StrategyGetter strategyGetter;

    /**根据请求信息返回推荐结果
     * @param request
     * @return
     */
    @Override
    public Map<String, CrossRecResponse> crossRecommend(CrossRecRequest request) {
        long startTime = System.currentTimeMillis();
        JSONObject jsodLog = new JSONObject();

        //收集请求日志
        LogCollection.CollectReq(jsodLog, request);
        Map<String, CrossRecResponse> recResponseMap = new HashMap<String, CrossRecResponse>() ;
        // 参数检验
        if (request.getUserId() <= 0 || request.getRecSceneType() == null) {
            long costTime = System.currentTimeMillis() - startTime;
            logger.warn(AvailField.REQUEST_PARA_WARNING + request);
            logger.info(AvailField.CROSS_REC_TIME + costTime + "ms");
            logger.info(jsodLog + "");
            return recResponseMap;
        }
        String strategy = strategyGetter.getStrategyName(request);

        List<RecSceneTypeEnum> recSceneType = request.getRecSceneType();

        //为每种场景制定不同的推荐策略
        for (RecSceneTypeEnum recSceneTypeEnum : recSceneType){
            int recScene = -1;
            String recHotelOrTravel = "";
            try{
                recScene = recSceneTypeEnum.getValue();
            }catch(Exception e){
                logger.error(AvailField.SCENE_EXPLAIN_EXCEPTION, e);
            }
            CrossRecResponse response = new CrossRecResponse();
            //推荐场景判断，默认推酒店附近
            try{
                sceneClass = genScene.getScene(recScene);
                if (sceneClass == null){
                    logger.warn(AvailField.SCENE_DELIVER_ERROR_WARNING + request);
                    continue;
                }
                recHotelOrTravel = genScene.getType(recScene);
            }catch(Exception e){
                logger.error(AvailField.SCENE_EXPLAIN_EXCEPTION, e);
                long costTime = System.currentTimeMillis() - startTime;
                logger.info(AvailField.CROSS_REC_TIME + costTime + "ms");
                JMonitor.add(JMonitorKey.CROSS_REC_TIME, costTime);
                logger.info(jsodLog + "");
                return recResponseMap;
            }
            sceneClass.setStrategy(strategy);
            List<TravelOrHotelPoiInfo> poiList = sceneClass.recommendTravelOrHotel(jsodLog, request,response);
            List<Long> recList = new ArrayList<Long>();

            int factNum = poiList.size();
            for(int i =0; i< factNum ; i++){
                recList.add(poiList.get(i).getPoiId());
            }
            response.setTotalNumOfPoi(factNum);
            response.setPoiArrayList(recList);
            response.setStatus(ResponseStatus.OK.getValue());
            recResponseMap.put(recHotelOrTravel, response);
            LogCollection.CollectRes(jsodLog, response, recHotelOrTravel);
        }
        long costTime = (System.currentTimeMillis() - startTime);
        logger.info(AvailField.CROSS_REC_TIME + costTime + "ms");
        JMonitor.add(JMonitorKey.CROSS_REC_TIME, costTime);
        LogCollection.printJSON(jsodLog);

        return recResponseMap;
    }

}
