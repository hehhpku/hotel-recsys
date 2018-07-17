package com.meituan.hotel.rec.service.recall;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.constants.Constants;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.external.HotelStarService;
import com.meituan.hotel.rec.service.recall.common.RecallRequest;
import com.meituan.hotel.rec.service.recall.implement.*;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.*;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.loc2city.service.Loc2CityService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.*;

import javax.annotation.Resource;

/**
 * Author: hehuihui,
 * hehuihui@meituan.com Date: 3/15/16
 * 召回路由，决策走哪个召回策略
 */
public class RecallRouter {
    @Resource(name = "city-poi-recall-service")
    private CityPoiRecalller defaultRecaller;

    @Resource
    private BrandPoiRecaller brandPoiRecaller;

    @Resource
    private HighStarPoiRecaller highStarPoiRecaller;

    @Resource
    private HotelStarService hotelStarService;

    private static final Logger logger = RecUtils.getLogger(RecallRouter.class.getSimpleName());

    @Resource
    private CityHotPoiRecaller cityHotPoiRecaller;

    private static final String DEFAULT_RECALL_STRATEGY = "cityPoiRecallStrategy";

    private Map<String, String> strategyMap;

    private Map<String, IRecaller> stringIRecallerMap;

    private Map<RecServiceType, List<String>> strategyCandidateMap;

    public List<PoiEntry> recall(RecRequest request, JSONObject joLog){
        long start = System.currentTimeMillis();

        RecallRequest recallRequest = new RecallRequest(request);
        String recallStrategy;
        IRecaller recaller;
        List<String> candidateStrategies = new ArrayList<String>
                ( strategyCandidateMap.get(request.getServiceType()));

        //优先指定召回策略，否则选取第一个候选召回策略
        if (strategyMap.containsKey(request.getStrategy())){
            recallStrategy = strategyMap.get(request.getStrategy());
        } else {
            recallStrategy = candidateStrategies.get(0);
        }

        // 详情页推荐，如果是高星酒店，则采用高星召回策略
        if (request.getServiceType() == RecServiceType.POI_DETAIL_REC) {
            List<Integer> stars = highStarPoiRecaller.getStarLevel(recallRequest);
            if (CollectionUtils.isNotEmpty(stars)) {
                recallStrategy = HighStarPoiRecaller.STRATEGY;
            }
        }

        List<PoiEntry> poiEntryList = new ArrayList<PoiEntry>();
        //搜索无结果推荐，采用专门设计的召回逻辑
        if (request.getServiceType() == RecServiceType.SEARCH_REC
                && CollectionUtils.isEmpty(request.getPoiOnShowList())){
            poiEntryList = recallForSearchNoResult(request);
        } else {
            while (CollectionUtils.isEmpty(poiEntryList)) {
                recaller = MapUtils.getObject(stringIRecallerMap, recallStrategy, defaultRecaller);
                RecResponse response = recaller.recall(recallRequest, joLog);
                if (response.getServiceStatus() == RecServiceStatus.OK) {
                    poiEntryList = response.getPoiEntryList();
                }
                //准备下一个召回策略
                try {
                    joLog.put("strategy_recall", recallStrategy);
                } catch (Exception e) {
                    logger.warn(RecUtils.getWarnString("json()"), e);
                }
                candidateStrategies.remove(recallStrategy);
                if (CollectionUtils.isEmpty(poiEntryList)) {
                    if (CollectionUtils.isEmpty(candidateStrategies)) {
                        break;
                    } else {
                        recallStrategy = candidateStrategies.get(0);
                    }
                }
            }
        }

        //搜索推荐：取品牌召回策略的结果，再融合其它策略结果
        if (RecServiceType.SEARCH_REC == request.getServiceType()){
            RecResponse response = brandPoiRecaller.recall(recallRequest, joLog);
            if (RecServiceStatus.OK == response.getServiceStatus()
                    && CollectionUtils.isNotEmpty(response.getPoiEntryList())){
                //将附近召回的结果加在品牌召回结果的后面，而不是前面，因为有属性记录poi的召回来源，在细排时用到
                List<PoiEntry> similarPoiEntryList = response.getPoiEntryList();
                similarPoiEntryList.addAll(poiEntryList);
                poiEntryList = similarPoiEntryList;
            }
        }

        //去重并过滤已展示的poi
        poiEntryList = RecUtils.convert2DistinctList(poiEntryList);
        if (CollectionUtils.isNotEmpty(recallRequest.getBlackPoiSet())){
            List<PoiEntry> poiEntryListFilterOnShow = new ArrayList<PoiEntry>();
            Set<Integer> poiOnShowSet = recallRequest.getBlackPoiSet();
            for (PoiEntry e: poiEntryList){
                if (!poiOnShowSet.contains(e.getPoiId())){
                    poiEntryListFilterOnShow.add(e);
                }
            }
            poiEntryList = poiEntryListFilterOnShow;
        }

        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.RECALL_ROUTER_TIME, timeCost);
        logger.info(RecUtils.getTimeCostString("RecallRouter()", timeCost));
        return poiEntryList;
    }

    public void setStrategyMap(Map<String, String> strategyMap) {
        this.strategyMap = strategyMap;
    }

    public void setStringIRecallerMap(Map<String, IRecaller> stringIRecallerMap) {
        this.stringIRecallerMap = stringIRecallerMap;
    }

    public void setStrategyCandidateMap(Map<RecServiceType, List<String>> strategyCandidateMap) {
        this.strategyCandidateMap = strategyCandidateMap;
    }

    /**
     * 搜索无结果推荐的召回，逻辑复杂，单独拿出来
     * 无结果推荐时，查找百度地图无结果，先用用户请求位置分本异地
     * 百度有结果，但召回无结果，用百度查回来的坐标查城市，利用该城市召回
     * @param request
     * @return
     */
    public List<PoiEntry> recallForSearchNoResult(RecRequest request){
        List<PoiEntry> poiEntryList = new ArrayList<PoiEntry>();
        RecallRequest recallRequest = new RecallRequest(request);
        // 百度查询回来的经纬度为空，且前端有传用户位置
        if (request.getRequestLocation() == null
                && RecDistanceUtils.isValidLocation(request.getUserLocation())){
            Location userLocation = request.getUserLocation();
            request.setRequestLocation(userLocation);
            int userCityId = request.getUserLocationCityId();
            //用户在本地搜索本地，查找附近酒店；本地搜索异地case待后面用热门推荐
            if (userCityId == request.getChannelCityId()){
                recallRequest.setChannelCityId(userCityId);
                recallRequest.setRequestLocation(userLocation);
                poiEntryList = recallViaCityPoi(recallRequest);
            }
        } else if (request.getRequestLocation() != null){ // 百度查询得到经纬度结果
            poiEntryList = recallViaCityPoi(recallRequest);
            //没召回结果，召回城市可能错误；改用百度查询回来的坐标对应的城市再查询一次
            if (CollectionUtils.isEmpty(poiEntryList)
                    && RecDistanceUtils.isValidLocation(request.getBaiduLocation())){
                Location location = request.getBaiduLocation();
                int baiduCityId = Loc2CityService.getInstance().loc2city(
                        (float)location.getLattitude(), (float)location.getLongitude());
                //目标城市与百度查询的城市不一样才需要重新查询
                if (baiduCityId > 0 && baiduCityId != request.getChannelCityId()) {
                    recallRequest.setChannelCityId(baiduCityId);
                    poiEntryList = recallViaCityPoi(recallRequest);
                }
            }
        }

        if (CollectionUtils.isEmpty(poiEntryList)){
            recallRequest.setChannelCityId(request.getChannelCityId());
            RecResponse response = cityHotPoiRecaller.recall(recallRequest, new JSONObject());
            if (response.getServiceStatus() == RecServiceStatus.OK){
                poiEntryList = response.getPoiEntryList();
            }
        }

        return poiEntryList;
    }

    /**
     * 查询城市附近poi的召回
     * @param request
     * @return
     */
    public List<PoiEntry> recallViaCityPoi(RecallRequest request){
        List<PoiEntry> poiEntryList = new ArrayList<PoiEntry>();
        RecResponse recResponse = defaultRecaller.recall(request, new JSONObject());
        if (recResponse.getServiceStatus() == RecServiceStatus.OK){
            poiEntryList = recResponse.getPoiEntryList();
        }
        return poiEntryList;
    }

}

