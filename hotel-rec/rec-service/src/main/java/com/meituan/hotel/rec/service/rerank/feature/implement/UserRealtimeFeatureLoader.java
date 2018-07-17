package com.meituan.hotel.rec.service.rerank.feature.implement;

import com.meituan.hotel.rec.service.common.Tuple;
import com.meituan.hotel.rec.service.constants.MedisClient;
import com.meituan.hotel.rec.service.rerank.feature.FeatureName;
import com.meituan.hotel.rec.service.rerank.feature.FeatureUtils;
import com.meituan.hotel.rec.service.rerank.feature.IFeatureLoader;
import com.meituan.hotel.rec.service.utils.RecDateUtils;
import com.meituan.service.mobile.util.CollectionUtil;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hehuihui on 3/25/16
 */
@Service("user-real-time-feature-loader")
public class UserRealtimeFeatureLoader implements IFeatureLoader {

    @Override
    public Map<Integer, Map<String,Double>> loadFeature(List<Integer> poiIdList, long userId){
        Map<Integer,Map<String,Double>> map = new HashMap<Integer,Map<String,Double>>();

        if (CollectionUtils.isEmpty(poiIdList)){
            return map;
        }
        //初始化特征
        List<String> featureList = Arrays.asList(FeatureName.VIEW_PAY_PROBABILITY);
        Map<String, Double> emptyFeatureMap = FeatureUtils.getEmptyFeatureMapFromList(featureList, 0.0);
        for (int poiId : poiIdList){
            map.put(poiId, emptyFeatureMap);
        }

        if (userId <= -1)
            return map;

        Map<Integer,Tuple> viewTupleMap = getPoiUserRecentView(userId);

        //获取浏览poi->支付poi的次数
        List<String> keyList = new ArrayList<String>();
        for (int poiId: viewTupleMap.keySet()){
            keyList.add(MedisClient.MEDIS_POI_V2P_PREFIX + poiId);
        }

        //获取浏览poiA->购买poiB的概率
        Map<String, String> poiV2StringMap = MedisClient.medisClient.multiGetString(keyList);
        Map<PoiEntryPair, Double> v2pEntryMap = new HashMap<PoiEntryPair, Double>();
        for (int poiView: viewTupleMap.keySet()){
            String key = MedisClient.MEDIS_POI_V2P_PREFIX + poiView;
            if (poiV2StringMap.containsKey(key)){
                String v2pString = poiV2StringMap.get(key);
                if (v2pString != null) {
                    for (String info : v2pString.split(",")) {
                        String[] temp = info.split("=");
                        int payPoi = Integer.parseInt(temp[0]);
                        double probability = Double.parseDouble(temp[1]);
                        v2pEntryMap.put(new PoiEntryPair(poiView, payPoi), probability);
                    }
                }
            }
        }

        //根据浏览时间排序，由近至远， 并截取前15个
        List<Tuple> viewTupleList = new ArrayList<Tuple>(viewTupleMap.values());
        Collections.sort(viewTupleList, Collections.reverseOrder(new Comparator<Tuple>() {
            @Override
            public int compare(Tuple o1, Tuple o2) {
                return Long.valueOf(o1.viewTimestamp).compareTo(o2.viewTimestamp);
            }
        }));
        int truncationNum = 15;
        viewTupleList = CollectionUtil.subList(viewTupleList, 0, truncationNum);

        //计算概率值
        for (int poiRec: poiIdList){
            Map<String, Double> featureValueMap = new HashMap<String, Double>();
            double probability = 0.0;
            for (Tuple tuple: viewTupleList){
                int poiView = tuple.poiId;
                int viewTimes = tuple.viewTimes;
                long timeDiff = System.currentTimeMillis()/1000 - tuple.viewTimestamp ;
                PoiEntryPair e = new PoiEntryPair(poiView, poiRec);
                if (timeDiff < RecDateUtils.TOW_DAY_SECOND && v2pEntryMap.containsKey(e)){
                    probability += v2pEntryMap.get(e) * Math.min(2, viewTimes);
                }
            }
            featureValueMap.put("V2P",probability);
            map.put(poiRec,featureValueMap);
        }

        return map;
    }

    /**
     * 获取用户实时浏览数据
     * @param userId 用户id
     * @return (poiId -> Tuple)
     */
    public static Map<Integer, Tuple> getPoiUserRecentView(long userId){
        Map<Integer,Tuple> viewTupleMap = new HashMap<Integer,Tuple>();
        if (userId <= 0L){
            return viewTupleMap;
        }

        //获取用户最近浏览数据
        String userKey = MedisClient.MEDIS_USER_REALTIME_PREFIX + userId;
        List<String> userRealTimeViewInfo = MedisClient.medisClient.lrangeObject(userKey,0,30); //取回最近30条浏览记录
        if (CollectionUtils.isEmpty(userRealTimeViewInfo))
            return viewTupleMap;

        //解析user最近浏览行为,(poiId -> tuple)
        for (String info: userRealTimeViewInfo){
            String[] poiViewInfo = info.split("_");
            Tuple tuple = new Tuple(Integer.parseInt(poiViewInfo[0]), RecDateUtils.parseTimeString(poiViewInfo[1]));
            if (viewTupleMap.containsKey(tuple.poiId)){
                viewTupleMap.get(tuple.poiId).updateTimeStamp(tuple.viewTimestamp);
            } else {
                viewTupleMap.put(tuple.poiId, tuple);
            }
        }
        return viewTupleMap;
    }

    static class PoiEntryPair {
        private int poiView;
        private int poiPay;

        public PoiEntryPair(int poiPay, int poiView) {
            this.poiPay = poiPay;
            this.poiView = poiView;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PoiEntryPair poiEntry = (PoiEntryPair) o;

            if (poiView != poiEntry.poiView) return false;
            return poiPay == poiEntry.poiPay;

        }

        @Override
        public int hashCode() {
            int result = poiView;
            result = 31 * result + poiPay;
            return result;
        }
    }
}
