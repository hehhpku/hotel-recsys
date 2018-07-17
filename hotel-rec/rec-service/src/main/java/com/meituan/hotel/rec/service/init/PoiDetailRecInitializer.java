package com.meituan.hotel.rec.service.init;

import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.rawRank.PoiFeatureGetter;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.HotelRecRequest;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.hotel.rec.thrift.PoiDetailRecExtraMsg;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 4/13/16
 */
@Service("poidetail-rec-initializer")
public class PoiDetailRecInitializer extends AbstractInitializer {
    @Resource
    private PoiFeatureGetter poiFeatureGetter;

    @Override
    public void addInfo2Req(RecRequest request, HotelRecRequest hotelRecRequest, JSONObject joLog) {
        List<Integer> poiOnShow = request.getPoiOnShowList();
        if (CollectionUtils.isNotEmpty(poiOnShow)){
            int poiId = poiOnShow.get(0);
            PoiEntry entry = poiFeatureGetter.getPoiInfo(Arrays.asList(poiId)).get(poiId);
            //城市
            List<Integer> poiCityId = entry.getPoiCityIds();
            request.setPoiCityIdList(poiCityId);
            //位置
            Location poiLocation = entry.getLocation();
            if (RecDistanceUtils.isValidLocation(poiLocation)) {
                request.setRequestLocation(poiLocation);
            }
            //价格
            double poiLowestPrice = entry.getLowestPrice();
            request.setPriceLow(poiLowestPrice);

        }
    }
}
