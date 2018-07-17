package com.meituan.hotel.rec.service.rawRank;

import com.alibaba.fastjson.*;
import com.meituan.hotel.rec.service.common.PoiEntry;
import com.meituan.hotel.rec.service.constants.MedisClient;
import com.meituan.hotel.rec.service.external.SinaiPoiClient;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.mobile.sinai.base.common.PoiFields;
import com.meituan.service.mobile.sinai.client.model.PoiModel;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

import javax.annotation.Resource;

/**
 * Created by jiangweisen, zuolin and hehuihui on 3/23/16
 */
@Service("poi-feature-getter")
public class PoiFeatureGetter {
    private static final Logger logger = RecUtils.getLogger(PoiFeatureGetter.class.getSimpleName());

    @Resource
    private SinaiPoiClient sinaiPoiClient;

    public static final List<String> FIELDS = Arrays.asList(
            PoiFields.lowestPrice,
            PoiFields.historyCouponCount,
            PoiFields.avgScore,
            PoiFields.markNumbers,
            PoiFields.lat,
            PoiFields.lng,
            PoiFields.cityIds,
            PoiFields.brandId,
            PoiFields.dIds
            );

    public Map<Integer, PoiEntry> getPoiInfo(List<Integer> poiIds){
        Map<Integer, PoiEntry> map = new HashMap<Integer, PoiEntry>();
        if (CollectionUtils.isEmpty( poiIds)){
            return map;
        }

        Set<Integer> poiWithoutFeature = new HashSet<Integer>(poiIds);

        //medis
        List<String> keys = new ArrayList<String>(poiIds.size());
        for (int poiId: poiIds){
            keys.add(MedisClient.POI_INFO_PREFIX_NEW + poiId);
        }
        Map<String, String> valueMap = MedisClient.medisClient.multiGetString(keys);
        for (int poiId: poiIds){
            try {
                String key = MedisClient.POI_INFO_PREFIX_NEW + poiId;
                if (!valueMap.containsKey(key) || valueMap.get(key) == null) {
                    continue;
                }
                JSONArray ja = JSON.parseArray(valueMap.get(key));
                PoiEntry entry = new PoiEntry(poiId);
                entry.setAvgScore(ja.getDouble(MedisClient.POI_AVG_SCORE));
                entry.setBrandId(ja.getInteger(MedisClient.POI_BRADNDID));
                entry.setLowestPrice(ja.getDouble(MedisClient.POI_LOWEST_PRICE));
                entry.setMarkNumbers(ja.getInteger(MedisClient.POI_MARK_NUM));
                double lat = ja.getDouble(MedisClient.POI_LAT);
                double lng = ja.getDouble(MedisClient.POI_LNG);
                entry.setLocation(new Location(lat, lng));
                JSONArray jaDeal = ja.getJSONArray(MedisClient.POI_DID);
                List<Integer> dealList = new ArrayList<Integer>();
                for (int index = 0; index < jaDeal.size(); index ++){
                    dealList.add(jaDeal.getInteger(index));
                }
                entry.setDealIdList(dealList);
                JSONArray jaCity = ja.getJSONArray(MedisClient.POI_CITYIDS);
                List<Integer> cityIds = new ArrayList<Integer>();
                for (int index = 0; index < jaCity.size(); index ++){
                    cityIds.add(jaCity.getInteger(index));
                }
                entry.setPoiCityIds(cityIds);
                entry.setSales(ja.getInteger(MedisClient.POI_SALE_NUM));

                map.put(poiId, entry);
                poiWithoutFeature.remove(poiId);
            } catch (Exception e){
                logger.error(RecUtils.getErrorString("getPoiInfoMEDIS"), e);
            }
        }

        //sinai
        if (poiWithoutFeature.size() > 0){
            try{
                List<PoiModel> poiModelList = sinaiPoiClient.multiThreadGetPoiModel(new ArrayList<Integer>(poiWithoutFeature), FIELDS, 100);
                for (PoiModel poiModel: poiModelList){
                    PoiEntry entry = new PoiEntry(poiModel);
                    map.put(entry.getPoiId(), entry);
                    poiWithoutFeature.remove(entry.getPoiId());
                }
            } catch (Exception e){
                logger.error(RecUtils.getErrorString("getPoiInfoSINAI"), e);
            }
        }

        if (CollectionUtils.isNotEmpty(poiWithoutFeature)){
            logger.warn(RecUtils.getWarnString("unfoundPoi=" + poiWithoutFeature));
            for (int poiId: poiWithoutFeature){
                map.put(poiId, new PoiEntry(poiId));
            }
        }

        return map;
    }
}
