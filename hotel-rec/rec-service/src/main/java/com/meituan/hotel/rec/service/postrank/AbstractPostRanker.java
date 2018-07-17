package com.meituan.hotel.rec.service.postrank;

import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.PoiSource;
import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.external.PromotionPoiService;
import com.meituan.hotel.rec.thrift.SortingMethod;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by hehuihui on 5/9/16
 */
public abstract class AbstractPostRanker {
    public static final Random random = new Random();

    public abstract List<PoiEntry> postRank(List<PoiEntry> poiEntryList, RecRequest request, JSONObject joLog);

    /**
     * 根据要求对候选poi排序
     * @param poiEntryList
     * @param sortingMethod
     * @return
     */
    protected List<PoiEntry> sortWithRequest(List<PoiEntry> poiEntryList, SortingMethod sortingMethod){

        //智能排序，加入运营置顶某些poi的功能
        if (CollectionUtils.isEmpty(poiEntryList) || sortingMethod == SortingMethod.SMART) {
            Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
                @Override
                public int compare(PoiEntry o1, PoiEntry o2) {
                    return AbstractPostRanker.compareSourceAndDistanceUPS(o1, o2);
                }
            }));
            poiEntryList = sortPoiWithPromotion(poiEntryList);
            return poiEntryList;
        }

        if (sortingMethod == SortingMethod.DISTANCE){
            Collections.sort(poiEntryList, new Comparator<PoiEntry>() {
                @Override
                public int compare(PoiEntry o1, PoiEntry o2) {
                    return Double.valueOf(o1.getDistanceToRequest()).compareTo(o2.getDistanceToRequest());
                }
            });
        } else if (sortingMethod == SortingMethod.SOLD) {
            Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
                @Override
                public int compare(PoiEntry o1, PoiEntry o2) {
                    if (o1.getSource() != o2.getSource() || o1.getDistanceToRequestUPS() != o2.getDistanceToRequestUPS()){
                        return AbstractPostRanker.compareSourceAndDistanceUPS(o1, o2);
                    } else {
                        return Integer.valueOf(o1.getMarkNumbers()).compareTo(o2.getMarkNumbers());
                    }
                }
            }));
        } else if (sortingMethod == SortingMethod.AVG_SCORE){
            Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
                @Override
                public int compare(PoiEntry o1, PoiEntry o2) {
                    if (o1.getSource() != o2.getSource() || o1.getDistanceToRequestUPS() != o2.getDistanceToRequestUPS()){
                        return AbstractPostRanker.compareSourceAndDistanceUPS(o1, o2);
                    } else {
                        return Double.valueOf(o1.getAvgScore()).compareTo(o2.getAvgScore());
                    }
                }
            }));
        } else if (sortingMethod == SortingMethod.LOWEST_PRICE){
            Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
                @Override
                public int compare(PoiEntry o1, PoiEntry o2) {
                    if (o1.getSource() != o2.getSource() || o1.getDistanceToRequestUPS() != o2.getDistanceToRequestUPS()){
                        return AbstractPostRanker.compareSourceAndDistanceUPS(o1, o2);
                    } else {
                        return Double.valueOf(o2.getLowestPrice()).compareTo(o1.getLowestPrice());
                    }
                }
            }));
        } else if (sortingMethod == SortingMethod.HIGH_PRICE){
            Collections.sort(poiEntryList, Collections.reverseOrder(new Comparator<PoiEntry>() {
                @Override
                public int compare(PoiEntry o1, PoiEntry o2) {
                    if (o1.getSource() != o2.getSource() || o1.getDistanceToRequestUPS() != o2.getDistanceToRequestUPS()){
                        return AbstractPostRanker.compareSourceAndDistanceUPS(o1, o2);
                    } else {
                        return Double.valueOf(o1.getLowestPrice()).compareTo(o2.getLowestPrice());
                    }
                }
            }));
        }
        return poiEntryList;
    }

    /**
     * 比较数据源，热门酒店排在最后
     * @param s1
     * @param s2
     * @return
     */
    public static int compareSource(PoiSource s1, PoiSource s2){
        if (s2 == PoiSource.CITY_HOT)
            return 1;
        else
            return -1;
    }

    public static int compareSourceAndDistanceUPS(PoiEntry o1, PoiEntry o2){
        if (o1.getSource() != o2.getSource()
                && (o1.getSource() == PoiSource.CITY_HOT || o2.getSource() == PoiSource.CITY_HOT)){
            return compareSource(o1.getSource(), o2.getSource());
        } else{
            return Integer.valueOf(o2.getDistanceToRequestUPS()).compareTo(o1.getDistanceToRequestUPS());
        }
    }

    /**
     * 加入运营poi置顶功能
     * @param poiEntryList
     * @return
     */
    private static List<PoiEntry> sortPoiWithPromotion(List<PoiEntry> poiEntryList){
        //判断是否有运营poi
        Map<Integer, Double> map = PromotionPoiService.getInstance().getPoiMap();
        Map<Integer, Double> poiOnPromotion = new HashMap<Integer, Double>();
        for (PoiEntry e: poiEntryList){
            int poiId = e.getPoiId();
            if (map.containsKey(poiId)){
                poiOnPromotion.put(poiId, map.get(poiId));
                break;
            }
        }
        //包含运营置顶poi， 选择一个置顶
        if (MapUtils.isNotEmpty(poiOnPromotion)) {
            int size = poiOnPromotion.size();
            int randomNum = random.nextInt(size);
            int poiIdOnTop = new ArrayList<Integer>(poiOnPromotion.keySet()).get(randomNum);
            Map<PoiEntry, Double> poiOrderMap = new HashMap<PoiEntry, Double>();
            for (int idx = 0; idx < poiEntryList.size(); idx++) {
                PoiEntry e = poiEntryList.get(idx);
                int poiId = e.getPoiId();
                if (poiId == poiIdOnTop) {
                    poiOrderMap.put(e, -1.0);
                } else {
                    poiOrderMap.put(e, (double) idx);
                }
            }
            List<Map.Entry<PoiEntry, Double>> poiOrderList =
                    new ArrayList<Map.Entry<PoiEntry, Double>>(poiOrderMap.entrySet());
            Collections.sort(poiOrderList, new Comparator<Map.Entry<PoiEntry, Double>>() {
                @Override
                public int compare(Map.Entry<PoiEntry, Double> o1, Map.Entry<PoiEntry, Double> o2) {
                    return Double.valueOf(o1.getValue()).compareTo(o2.getValue());
                }
            });
            List<PoiEntry> poiEntryWithPromotion = new ArrayList<PoiEntry>();
            for(Map.Entry<PoiEntry, Double> e: poiOrderList){
                poiEntryWithPromotion.add(e.getKey());
            }
            poiEntryList = poiEntryWithPromotion;
        }
        return poiEntryList;
    }
}


