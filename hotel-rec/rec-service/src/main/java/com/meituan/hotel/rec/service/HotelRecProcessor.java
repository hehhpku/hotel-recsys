package com.meituan.hotel.rec.service;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.constants.*;
import com.meituan.hotel.rec.service.filter.Filter;
import com.meituan.hotel.rec.service.init.RecInitializer;
import com.meituan.hotel.rec.service.log.RecLog;
import com.meituan.hotel.rec.service.pack.Packer;
import com.meituan.hotel.rec.service.postrank.PostRankRouter;
import com.meituan.hotel.rec.service.rawRank.*;
import com.meituan.hotel.rec.service.recall.RecallRouter;
import com.meituan.hotel.rec.service.rerank.RerankRouter;
import com.meituan.hotel.rec.service.utils.*;
import com.meituan.hotel.rec.thrift.*;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.mtthrift.server.MTIface;
import com.meituan.service.mobile.util.CollectionUtil;

import org.apache.thrift.TException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import javax.annotation.Resource;

/**
 * Author: hehuihui@meituan.com Date: 3/14/16
 */
public class HotelRecProcessor extends MTIface implements HotelRecService.Iface {

    private static final Logger logger = RecUtils.getLogger(HotelRecProcessor.class.getSimpleName());

    @Autowired
    private RecallRouter recallRouter;

    @Resource
    private RawRankRouter rawRankRouter;

    @Resource(name = "filter-service")
    private Filter filter;

    @Resource
    private Packer packer;

    @Autowired
    private RerankRouter rerankRouter;

    @Autowired
    private RecLog recLog;

    @Autowired
    private RecInitializer recInitializer;

    @Autowired
    private PostRankRouter postRankRouter;

    @Override
    public HotelRecResponse recommend(HotelRecRequest hotelRecRequest) throws TException {
        long start = System.currentTimeMillis();
        logger.info(RecUtils.getInfoString(hotelRecRequest.toString()));

        HotelRecResponse response = new HotelRecResponse();

        List<PoiEntry> poiEntryList = new ArrayList<PoiEntry>();
        JSONObject joLog = new JSONObject();
        RecRequest request = new RecRequest();
        if (isValidRequest(hotelRecRequest)){
            try{
                request = recInitializer.initRequest(hotelRecRequest, joLog);
                logger.info(RecUtils.getInfoString(request.toString()));
                poiEntryList = recallRouter.recall(request, joLog);
                poiEntryList = rawRankRouter.rawRank(poiEntryList, request, joLog);
                poiEntryList = rerankRouter.rerank(request, poiEntryList, joLog);
                poiEntryList = filter.filter(request, poiEntryList, joLog);
                poiEntryList = postRankRouter.postRank(poiEntryList, request, joLog);
                poiEntryList = packer.pack(poiEntryList, request, joLog);
                poiEntryList = truncateResult(poiEntryList, request.getServiceType());
            } catch (Exception e){
                logger.error(RecUtils.getErrorString("FATAL") + e.getMessage() + hotelRecRequest.toString(), e);
                JMonitor.add(JmonitorKey.FATAL_ERROR);
            }
        }

        List<PoiRecInfo> poiRecInfoList = new ArrayList<PoiRecInfo>();
        for (PoiEntry e: poiEntryList){
            poiRecInfoList.add(e.convert2PoiRecInfo());
        }

        response.setPoiRecList(poiRecInfoList);
        response.setRecNum(poiRecInfoList.size());
        response.setServiceStatus(RecServiceStatus.OK);
        response.setStrategy(request.getStrategy());

        recLog.printLog2Flume(request, hotelRecRequest, response, joLog, poiEntryList);

        //搜索推荐设置距离，只有当结果数为1时才返回距离，后期应该修改逻辑
        //此段逻辑放在打印日志后面，因为我们需要在日志中记录这个距离信息
        if (RecServiceType.SEARCH_REC == hotelRecRequest.getServiceType()
                && hotelRecRequest.getPoiOnShow() != null
                && hotelRecRequest.getPoiOnShow().size() != 1){
            for (PoiRecInfo e: response.getPoiRecList()){
                e.setDistanceToRequest(-1.0);
                e.setDistanceToUser(-1.0);
            }
        }

        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.SERVICE_TIME, timeCost);
        JMonitor.add(JmonitorKey.getTimeKey(hotelRecRequest.getServiceType().name()), timeCost);
        logger.info(RecUtils.getInfoString("strategy=" + response.getStrategy() + ",num="
                + response.getPoiRecListSize() +",TOP3="
                + CollectionUtil.subList(response.getPoiRecList(), 0, 3)));
        logger.info(RecUtils.getTimeCostString("recommend_service", timeCost));
        //服务耗时大于200ms，方便查看耗时过大的数目
        if (timeCost > 200){
            JMonitor.add(JmonitorKey.TIMEOUT_200_MS);
        }
        return response;
    }

    /**
     * 判断是否一个合法请求
     * @param request
     * @return
     */
    private boolean isValidRequest(HotelRecRequest request){
        return request != null && request.isSetServiceType();
    }

    /**
     * 根据需要设置返回给api的结果数
     * @param poiEntryList
     * @param type
     * @return
     */
    private List<PoiEntry> truncateResult(List<PoiEntry> poiEntryList, RecServiceType type){
        if (RecServiceType.SELECT_REC == type){
            return CollectionUtil.subList(poiEntryList, 0, 20);
        } else if (RecServiceType.POI_DETAIL_REC == type || RecServiceType.REC_PREORDER_POI == type){
            return CollectionUtil.subList(poiEntryList, 0, 35);
        } else if (RecServiceType.SEARCH_REC == type){
            return CollectionUtil.subList(poiEntryList, 0, 30);
        } else {
            return poiEntryList;
        }
    }
}
