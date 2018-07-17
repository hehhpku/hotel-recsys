package com.meituan.hotel.rec.service.rerank.feature;

import java.util.*;

/**
 * Created by hehuihui on 3/25/16
 */
public interface IFeatureLoader {
    Map<Integer, Map<String, Double>> loadFeature(List<Integer> poiIdList, long userId);
}
