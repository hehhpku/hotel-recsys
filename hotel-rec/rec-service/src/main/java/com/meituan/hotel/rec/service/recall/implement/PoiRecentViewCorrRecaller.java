package com.meituan.hotel.rec.service.recall.implement;

import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.RecResponse;
import com.meituan.hotel.rec.service.common.Tuple;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.constants.MedisClient;
import com.meituan.hotel.rec.service.rawRank.PoiFeatureGetter;
import com.meituan.hotel.rec.service.recall.IRecaller;
import com.meituan.hotel.rec.service.recall.common.RecallRequest;
import com.meituan.hotel.rec.service.rerank.feature.implement.UserRealtimeFeatureLoader;
import com.meituan.hotel.rec.thrift.RecServiceStatus;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.util.CollectionUtil;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 5/31/16
 * 1. 获取用户最近浏览poi
 * 2. 查询这些poi的基础属性及关联的poi
 * 3. 过滤掉不符合的poi, 融合所有关联的poi
 */
@Service("poi-real-time-view-corr-recaller")
public class PoiRecentViewCorrRecaller implements IRecaller{
    public static final String STRATEGY = "poiRecentViewCorrRecallerStrategy";

    @Resource
    private PoiFeatureGetter poiFeatureGetter;

    @Override
    public RecResponse recall(RecallRequest request, JSONObject joLog) {
        long start = System.currentTimeMillis();

        RecResponse response = RecResponse.getErrorResponse();
        if (CollectionUtils.isEmpty(request.getPoiOnShow())){
            response.setStrategy(STRATEGY);
            response.setServiceStatus(RecServiceStatus.ERROR);
            return response;
        }

        //获取用户最近的15个浏览，当前poi作为最近的浏览
        long userId = request.getUserId();
        int poiId = request.getPoiOnShow().get(0);
        Map<Integer, Tuple> map = UserRealtimeFeatureLoader.getPoiUserRecentView(userId);
        if (map.containsKey(poiId)){
            map.get(poiId).updateTimeStamp(start / 1000);
        } else {
            map.put(poiId, new Tuple(poiId, start / 1000));
        }
        List<Tuple> viewTupleList = new ArrayList<Tuple>(map.values());
        Collections.sort(viewTupleList, Collections.reverseOrder(new Comparator<Tuple>() {
            @Override
            public int compare(Tuple o1, Tuple o2) {
                return Long.valueOf(o1.viewTimestamp).compareTo(o2.viewTimestamp);
            }
        }));
        int truncationNum = 15;
        viewTupleList = CollectionUtil.subList(viewTupleList, 0, truncationNum);

        //过滤掉不符合城市要求的poi，排除掉异地浏览的问题
        List<Integer> poiIdList = new ArrayList<Integer>();
        for (Tuple tuple: viewTupleList){
            poiIdList.add(tuple.poiId);
        }
        Set<Integer> poiViewedSet = new HashSet<Integer>();
        poiViewedSet.add(poiId);
        //是否有浏览行为
        if (poiIdList.size() > 1) {
            Map<Integer, PoiEntry> poiInfoMap = poiFeatureGetter.getPoiInfo(poiIdList);
            Set<Integer> citySet = new HashSet<Integer>();
            if (CollectionUtils.isNotEmpty(request.getPoiCityIdList())){
                citySet.addAll(request.getPoiCityIdList());
            }
            for (int poiIdTemp : poiIdList) {
                if (!poiInfoMap.containsKey(poiIdTemp) || poiIdTemp <= 0) {
                    continue;
                }
                PoiEntry e = poiInfoMap.get(poiIdTemp);
                //浏览的poi与当前poi在同一城市则保留，否则剔除
                if (CollectionUtils.isNotEmpty(e.getPoiCityIds())) {
                    for (int city : e.getPoiCityIds()) {
                        if (citySet.contains(city)) {
                            poiViewedSet.add(poiIdTemp);
                            break;
                        }
                    }
                }
                //只需要3个最近浏览且与当前poi在同一城市的poi
                if (poiViewedSet.size() >= 3){
                    break;
                }
            }
        }

        //获取关联的poi, 并根据关联分排序
        double baseLat = request.getRequestLocation().getLattitude();
        double baseLng = request.getRequestLocation().getLongitude();
        List<PoiEntry> poiEntryList = PoiCorrV2PRecaller.getPoiRelationItems(new ArrayList<Integer>(poiViewedSet),
                baseLng, baseLat, MedisClient.POI_V2P_LLR_CORR_PREFIX_KEY);

        Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
            @Override
            public int compare(PoiEntry o1, PoiEntry o2) {
                return Double.compare(o1.getCorrScore(), o2.getCorrScore());
            }
        }));

        response.setPoiEntryList(poiEntryList);
        response.setServiceStatus(RecServiceStatus.OK);
        response.setStrategy(STRATEGY);

        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.RECENT_VIEW_CORR_RECALL_TIME, timeCost);

        return response;
    }
}
