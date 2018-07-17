package com.meituan.hotel.rec.service.init;

import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.constants.Constants;
import com.meituan.hotel.rec.service.external.CityService;
import com.meituan.hotel.rec.service.utils.RecDistanceUtils;
import com.meituan.hotel.rec.service.utils.SearchUtil;
import com.meituan.hotel.rec.thrift.HotelRecRequest;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.hotel.rec.thrift.SearchRecExtraMsg;
import com.meituan.service.mobile.loc2city.service.Loc2CityService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 4/13/16
 */
@Service("search-rec-initializer")
public class SearchRecInitializer extends AbstractInitializer {

    @Resource
    private SearchUtil searchUtil;

    @Override
    public void addInfo2Req(RecRequest request, HotelRecRequest hotelRecRequest, JSONObject joLog) {

        SearchRecExtraMsg searchRecMsg = hotelRecRequest.getSearchRecMsg();
        if (searchRecMsg != null) {
            //query
            String query = searchRecMsg.getQuery();
            if (query.length() > Constants.MAX_QUERY_LENGTH) {
                query = StringUtils.substring(query, 0, Constants.MAX_QUERY_LENGTH);
            }
            request.setQuery(query);

            //尝试城市改写
            String channelCityName = searchRecMsg.getCityName();
            List<Integer> poiOnShow = request.getPoiOnShowList();
            if (CollectionUtils.isEmpty(poiOnShow)){
                channelCityName = SearchUtil.getTargetCity(channelCityName, query);
            }
            //处理县级市问题
            if (SearchUtil.getRewriteCityMap().containsKey(channelCityName)) {
                channelCityName = SearchUtil.getRewriteCityMap().get(channelCityName);
            }
            request.setCityName(channelCityName);

            int channelCityId = CityService.getInstance().getIdByName(channelCityName);
            request.setChannelCityId(channelCityId);

            //场景
            int sceneId = SearchUtil.rewriteSceneId(searchRecMsg.getSceneId(), poiOnShow.size());
            request.setSceneId(sceneId);

            //用户定位
            Location userLocation = request.getUserLocation();
            int userCityId = Loc2CityService.getInstance()
                    .loc2city((float) userLocation.getLattitude(), (float) userLocation.getLongitude());
            request.setUserLocationCityId(userCityId);
            Map<String, String> infoMap = new HashMap<String, String>();
            Location requestLocation = searchUtil.getLocationFromQueryOrSearchResult(request, query, channelCityName, poiOnShow, userLocation, channelCityId, infoMap);
            request.setRequestLocation(requestLocation);

            //第一个搜索结果的最低价格，用在后面粗排
            double firstSearchPoiPrice = 200.0;
            if (infoMap.containsKey("firstPoiPrice")){
                double tempPrice = Double.parseDouble(infoMap.get("firstPoiPrice"));
                if (tempPrice > 0.001){
                    firstSearchPoiPrice = tempPrice;
                }
            }
            request.setSearchPoiPrice(firstSearchPoiPrice);
        }
    }
}
