package com.meituan.hotel.rec.client.common;

import java.util.Map;

/**
 * Created by jiangweisen on 3/31/16
 */
public class RecMapUtils {
    public static<T,S> void put(Map<T, S> map, Map.Entry<T, S> e){
        if (map == null){
            throw new NullPointerException(RecMapUtils.class.getSimpleName() + ", map is null");
        }
        if (e != null){
            map.put(e.getKey(), e.getValue());
        }
    }

    public static<T, S> void putALl(Map<T, S> mapDestination, Map<T, S> mapSource){
        if (mapDestination == null){
            throw new NullPointerException(RecMapUtils.class.getSimpleName() + ", map is null");
        }

        if (mapSource != null){
            mapDestination.putAll(mapSource);
        }
    }
}
