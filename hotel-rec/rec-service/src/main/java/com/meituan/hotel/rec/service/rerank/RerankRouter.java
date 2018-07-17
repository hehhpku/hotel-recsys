package com.meituan.hotel.rec.service.rerank;

import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.rerank.common.RerankRequest;
import com.meituan.hotel.rec.service.rerank.common.RerankResponse;
import com.meituan.hotel.rec.service.rerank.feature.FeatureName;
import com.meituan.hotel.rec.service.utils.RecDateUtils;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.hotel.rec.thrift.RecServiceType;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.util.CollectionUtil;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.*;

/**
 * Created by hehuihui on 3/25/16
 */
public class RerankRouter {
    private static final Logger logger = RecUtils.getLogger(RerankRouter.class.getSimpleName());

    private Map<String, IReranker> rerankerMap;

    public static final int NUM_RERANK = 50;

    private static Map<RecServiceType, OnlineFeatureGetter> featureGetterMap;

    static {
        initOnlineFeatureGetterMap();
    }

    public List<PoiEntry> rerank(RecRequest request, List<PoiEntry> poiEntryList, JSONObject joLog){
        long start = System.currentTimeMillis();
        if (CollectionUtils.isEmpty(poiEntryList)){
            return poiEntryList;
        }

        poiEntryList = CollectionUtil.subList(poiEntryList, 0, NUM_RERANK);

        Map<Integer,Map<String,Double>> featureValueMap = new HashMap<Integer, Map<String, Double>>();
        if (featureGetterMap.containsKey(request.getServiceType())){
            featureValueMap = featureGetterMap.get(request.getServiceType()).getOnlineFeature(request, poiEntryList);
        }
        String strategy = request.getStrategy();
        IReranker reranker;
        if (rerankerMap.containsKey(strategy)){
            reranker = rerankerMap.get(strategy);
            //封装rerank的请求
            RerankRequest rerankRequest = new RerankRequest();
            rerankRequest.setPoiIdsFeatureMap(featureValueMap);
            rerankRequest.setUserId(request.getUserId());

            //调用rerank服务
            RerankResponse rerankResponse = reranker.rerank(rerankRequest);

            //处理rerank结果
            Map<Integer, Double> poiIdsPredictScoreMap = new HashMap<Integer, Double>();
            if ( RecServiceStatus.OK == rerankResponse.getServiceStatus() ) {
                poiIdsPredictScoreMap = rerankResponse.getPoiidPredictScoreMap();
            } else {
                double score = poiEntryList.size();
                for ( PoiEntry e: poiEntryList ){
                    poiIdsPredictScoreMap.put(e.getPoiId(), score);
                    score -= 1;
                }
            }
            for (PoiEntry e : poiEntryList)
                e.setRerankScore(poiIdsPredictScoreMap.get(e.getPoiId()));
            //只根据rerank的得分来排序
            Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
                @Override
                public int compare(PoiEntry o1, PoiEntry o2) {
                    return Double.compare(o1.getRerankScore(), o2.getRerankScore());
                }
            }));
        }

        long timeCost = System.currentTimeMillis() - start;
        logger.info(RecUtils.getTimeCostString("RerankRouter()", timeCost));
        JMonitor.add(JmonitorKey.RERANK, timeCost);

        return poiEntryList;
    }

    public void setRerankerMap(Map<String, IReranker> rerankerMap) {
        this.rerankerMap = rerankerMap;
    }

    interface OnlineFeatureGetter{Map<Integer,Map<String,Double>> getOnlineFeature(RecRequest request, List<PoiEntry> poiEntryList);}

    public static void initOnlineFeatureGetterMap(){
        OnlineFeatureGetter selectRecGetter = new OnlineFeatureGetter() {
            @Override
            public Map<Integer, Map<String, Double>> getOnlineFeature(RecRequest request, List<PoiEntry> poiEntryList) {
                Map<Integer, Map<String,Double>> poiIdsFeatureMap = new HashMap<Integer, Map<String, Double>>();
                for (PoiEntry e: poiEntryList){
                    Map<String,Double> featureValueMap = new HashMap<String, Double>();
                    featureValueMap.put(FeatureName.DISTANCE_TO_USER_FEATURE, e.getDistanceToRequest());
                    poiIdsFeatureMap.put(e.getPoiId(), featureValueMap);
                }
                return poiIdsFeatureMap;
            }
        };

        OnlineFeatureGetter searchRecGetter = new OnlineFeatureGetter() {
            @Override
            public Map<Integer, Map<String, Double>> getOnlineFeature(RecRequest request, List<PoiEntry> poiEntryList) {
                int sceneId = request.getSceneId();
                Map<Integer, Map<String,Double>> poiIdsFeatureMap = new HashMap<Integer, Map<String, Double>>();
                int pos = 0;
                for (PoiEntry e: poiEntryList){
                    Map<String,Double> featureValueMap = new HashMap<String, Double>();
                    featureValueMap.put(FeatureName.SCENEID, (double)sceneId);
                    featureValueMap.put(FeatureName.DISTANCE, e.getDistanceToRequest());
                    featureValueMap.put(FeatureName.POSITION, (double)pos);
                    featureValueMap.put(FeatureName.TYPE, (double) getRecTypeFromName(e.getSource()));
                    poiIdsFeatureMap.put(e.getPoiId(), featureValueMap);
                    pos++;
                }
                return poiIdsFeatureMap;
            }
        };

        OnlineFeatureGetter poiDetailRecGetter = new OnlineFeatureGetter() {
            @Override
            public Map<Integer, Map<String, Double>> getOnlineFeature(RecRequest request, List<PoiEntry> poiEntryList) {
                Map<Integer, Map<String,Double>> poiIdsFeatureMap = new HashMap<Integer, Map<String, Double>>();
                int pos = 0;
                for (PoiEntry e: poiEntryList){
                    double distanceToUser = RecDistanceUtils.calDistance(request.getUserLocation(), e.getLocation());
                    e.setDistanceToUser(distanceToUser);
                    Map<String,Double> featureValueMap = new HashMap<String, Double>();
                    featureValueMap.put(FeatureName.DISTANCEE_TO_POI, e.getDistanceToRequest());
                    featureValueMap.put(FeatureName.DISTANCEE_TO_USER, e.getDistanceToUser());
                    featureValueMap.put(FeatureName.POSITION, Double.valueOf(pos));
                    featureValueMap.put(FeatureName.DAY_OF_WEEK, Double.valueOf(RecDateUtils.getDayOfWeek()));
                    featureValueMap.put(FeatureName.HOUR_OF_DAY, Double.valueOf(RecDateUtils.getHourOfDay()));
                    featureValueMap.put(FeatureName.CORRS, e.getCorrScore());
                    featureValueMap.put(FeatureName.DISTANCES, e.getDistanceScore());
                    featureValueMap.put(FeatureName.MARKS, e.getMarkScore());
                    featureValueMap.put(FeatureName.PRICES, e.getPriceScore());
                    featureValueMap.put(FeatureName.GMV, e.getSales() * e.getLowestPrice());
                    poiIdsFeatureMap.put(e.getPoiId(), featureValueMap);
                    pos ++;
                }
                return poiIdsFeatureMap;
            }
        };

        featureGetterMap = new HashMap<RecServiceType, OnlineFeatureGetter>();

        featureGetterMap.put(RecServiceType.SELECT_REC, selectRecGetter);
        featureGetterMap.put(RecServiceType.SEARCH_REC, searchRecGetter);
        featureGetterMap.put(RecServiceType.POI_DETAIL_REC, poiDetailRecGetter);
    }

    private static int getRecTypeFromName(PoiSource source){
        if(PoiSource.NEAR_BY == source)
            return 1;
        else if(PoiSource.SIM_BRAND == source)
            return 2;
        else
            return 0;
    }
}
