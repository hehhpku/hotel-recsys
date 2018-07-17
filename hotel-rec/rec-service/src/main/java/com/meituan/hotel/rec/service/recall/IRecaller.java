package com.meituan.hotel.rec.service.recall;

import com.meituan.hotel.rec.service.common.RecResponse;
import com.meituan.hotel.rec.service.recall.common.RecallRequest;

import org.json.JSONObject;

/**
 * Author: hehuihui@meituan.com
 * Date: 3/15/16
 */
public interface IRecaller {
    RecResponse recall(RecallRequest request, JSONObject joLog);
}
