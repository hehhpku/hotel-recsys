package com.meituan.hotel.rec.common.utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by jiangweisen on 3/31/16
 */
public class RecCollectionUtils {
    public static <T> List<T> subList(List<T> list, int offset, int limit) {
    if(list != null && !list.isEmpty()) {
        if(offset >= list.size()) {
            return Collections.emptyList();
        } else {
            if(offset < 0) {
                offset = 0;
            }

            if(limit <= 0) {
                limit = list.size();
            }

            int toIndex = offset + limit;
            if(toIndex > list.size()) {
                toIndex = list.size();
            }

            return list.subList(offset, toIndex);
        }
    } else {
        return Collections.emptyList();
    }
}
}
