package com.meituan.hotel.rec.service.rawRank;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.rawRank.implement.SimpleRawRanker;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.hotel.rec.thrift.RecServiceType;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.util.CollectionUtil;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 3/22/16
 */
@Service
public class RawRankRouter {
    private static final Logger logger = RecUtils.getLogger(RawRankRouter.class.getSimpleName());

    @Resource(name = "poi-feature-getter")
    private PoiFeatureGetter poiFeatureGetter;

    private Map<String, IRawRanker> strategyServiceMap;

    private Map<RecServiceType, IRawRanker> serviceTypeRawRankerMap;

    public static final int NUM_RAW_RANK_RETURN = 50;

    @Resource(name = "simple-raw-rank-service")
    private SimpleRawRanker defaultRawRanker;

    public List<PoiEntry> rawRank(List<PoiEntry> poiEntryList, RecRequest request, JSONObject joLog){
        long start = System.currentTimeMillis();
        if (CollectionUtils.isEmpty(poiEntryList)){
            return poiEntryList;
        }

        String strategy = request.getStrategy();

        //粗排策略优先级：策略指定 >> 服务类型默认策略 >> 不进行粗排
        IRawRanker rawRanker;
        if (strategy != null && strategyServiceMap.containsKey(strategy)){
            rawRanker = strategyServiceMap.get(strategy);
        } else if (serviceTypeRawRankerMap.containsKey(request.getServiceType())) {
            rawRanker = serviceTypeRawRankerMap.get(request.getServiceType());
        } else {
            rawRanker = defaultRawRanker;
        }

        try {
            //补充粗排特征
            updateRawRankFeature(poiEntryList);
            //粗排
            RecResponse recResponse = rawRanker.rawRank(request, poiEntryList, joLog);
            if (recResponse.getServiceStatus() == RecServiceStatus.OK) {
                poiEntryList = recResponse.getPoiEntryList();
            }
            //搜索推荐需要限制一下品牌酒店出现的个数
            if (RecServiceType.SEARCH_REC == request.getServiceType()){
                poiEntryList = filterBrandPoiInSearchRec(poiEntryList);
            }
        } catch (Exception e){
            logger.error(RecUtils.getErrorString("RawRankRouter()"), e);
        }

        poiEntryList = CollectionUtil.subList(poiEntryList, 0, NUM_RAW_RANK_RETURN);

        long timeCost = System.currentTimeMillis() - start;
        logger.info(RecUtils.getTimeCostString("rawRankRouter()", timeCost));
        JMonitor.add(JmonitorKey.RAW_RANK_ROUTER_TIME, timeCost);
        return poiEntryList;
    }

    /**
     * 对于没有粗排特征的poiEntry，补充粗排特征
     * @param poiEntryList
     */
    public void updateRawRankFeature(List<PoiEntry> poiEntryList){
        List<Integer> poiWithoutFeature = new ArrayList<Integer>();
        for (PoiEntry entry: poiEntryList){
            if (!entry.isSetFeature()){
                poiWithoutFeature.add(entry.getPoiId());
            }
        }

        if (CollectionUtils.isNotEmpty(poiWithoutFeature)){
            Map<Integer, PoiEntry> map = poiFeatureGetter.getPoiInfo(poiWithoutFeature);
            for (PoiEntry entry: poiEntryList){
                int poiId = entry.getPoiId();
                if (map.containsKey(poiId)){
                    entry.updateInfo(map.get(poiId));
                }
            }
        }
    }

    public void setStrategyServiceMap(Map<String, IRawRanker> strategyServiceMap) {
        this.strategyServiceMap = strategyServiceMap;
    }

    public void setServiceTypeRawRankerMap(Map<RecServiceType, IRawRanker> serviceTypeRawRankerMap) {
        this.serviceTypeRawRankerMap = serviceTypeRawRankerMap;
    }

    /**
     * 搜索推荐中，防止距离远的品牌poi出现太多，采用该方法限制数目
     * @param poiEntryList
     * @return
     */
    public static List<PoiEntry> filterBrandPoiInSearchRec(List<PoiEntry> poiEntryList){
        int similarPoiNum = 0;
        double filterDistance = 3000;
        List<PoiEntry> poiEntryListAfterFilter = new ArrayList<PoiEntry>();
        for (PoiEntry e: poiEntryList){
            if (PoiSource.SIM_BRAND == e.getSource()){
                if (e.getDistanceToRequest() <= filterDistance || similarPoiNum <= 4){
                    similarPoiNum ++;
                } else{
                    continue;
                }
            }
            poiEntryListAfterFilter.add(e);
        }

        return poiEntryListAfterFilter;
    }
}
