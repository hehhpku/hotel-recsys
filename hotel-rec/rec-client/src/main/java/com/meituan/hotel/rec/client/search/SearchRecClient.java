package com.meituan.hotel.rec.client.search;

import com.meituan.hotel.rec.client.HotelRecClient;
import com.meituan.hotel.rec.client.common.Utils;
import com.meituan.hotel.rec.client.select.SelectRecClient;
import com.meituan.hotel.rec.thrift.*;
import com.meituan.hotel.search.recommend.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.*;

import java.util.*;

/**
 * Author: jiangweisen,jiangweisen@meituan.com Date: 9/10/15
 */
public class SearchRecClient {

    private HotelRecClient hotelRecClient;

    public void setHotelRecClient(HotelRecClient hotelRecClient) {
        this.hotelRecClient = hotelRecClient;
    }

    @Deprecated
    public SearchRecResponse searchRecommend(SearchRecRequest searchRecRequest) throws TException{
        HotelRecRequest hotelRecRequest = transformRequest(searchRecRequest);
        HotelRecResponse hotelRecResponse = hotelRecClient.recommend(hotelRecRequest);
        SearchRecResponse searchRecResponse = transformResponse(hotelRecResponse);
        return searchRecResponse;
    }

    /**
     * 将搜索推荐的请求转化为rec的请求
     * @param searchRecRequest
     * @return
     */
    private static HotelRecRequest transformRequest(SearchRecRequest searchRecRequest){
        HotelRecRequest hotelRecRequest = new HotelRecRequest();
        hotelRecRequest.setServiceType(RecServiceType.SEARCH_REC);

        UserRecInfo userRecInfo = new UserRecInfo();
        SearchRecExtraMsg msg = new SearchRecExtraMsg();
        hotelRecRequest.setSearchRecMsg(msg);
        hotelRecRequest.setUserInfo(userRecInfo);

        String uuid = searchRecRequest.getUuid();
        userRecInfo.setUuid(uuid);

        String channelCityName = searchRecRequest.getCity();
        msg.setCityName(channelCityName);

        int offset = searchRecRequest.getOffset();
        hotelRecRequest.setOffset(offset);

        msg.setQuery(searchRecRequest.getQuery());

        String sortingMethod = searchRecRequest.getOrderby();
        hotelRecRequest.setSortingMethod(SelectRecClient.getSortingMethod(sortingMethod));

        String location = searchRecRequest.getLocation();
        userRecInfo.setUserLocation(parseLocation(location));

        int sceneId = 0;
        List<Integer> poiOnShow = new ArrayList<Integer>();
        if (searchRecRequest.isSetExtensions()){
            Map<String, String> map = searchRecRequest.getExtensions();
            Map<String, String> extraMap = new HashMap<String, String>(map);
            hotelRecRequest.setExtraDataMap(extraMap);
            try {
                //可以利用extension里配置策略
                if (map.containsKey("strategy")){
                    hotelRecRequest.setStrategy(map.get("strategy"));
                }
                if (map.containsKey("sceneId")) {
                    sceneId = Integer.parseInt(map.get("sceneId"));
                }
                if (map.containsKey("poiIds")){
                    poiOnShow = Utils.getIntListFromString(map.get("poiIds"), Utils.SEPERATOR_COMMA);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        msg.setSceneId(sceneId);
        hotelRecRequest.setPoiOnShow(poiOnShow);

        AccommodationType accType = AccommodationType.DR;
        if (searchRecRequest.isSetFilters()
                && searchRecRequest.getFilters().containsKey("attrID")
                && !"28:129;".equals(searchRecRequest.getFilters().get("attrID"))){
            accType = AccommodationType.HR;
        }
        userRecInfo.setAccType(accType);

        return hotelRecRequest;
    }

    /**
     * 将rec的结果转化为搜索推荐的结果
     * @param hotelRecResponse
     * @return
     */
    private static SearchRecResponse transformResponse(HotelRecResponse hotelRecResponse){
        SearchRecResponse searchRecResponse = new SearchRecResponse();

        //状态
        PoiResponseStatus status = PoiResponseStatus.OK;
        if (RecServiceStatus.OK != hotelRecResponse.getServiceStatus()){
            status = PoiResponseStatus.ERROR;
        }
        searchRecResponse.setStatus(status);
        //结果
        List<PoiRecEntry> poiRecEntries = new ArrayList<PoiRecEntry>();
        if (CollectionUtils.isNotEmpty(hotelRecResponse.getPoiRecList())) {
            for (PoiRecInfo info : hotelRecResponse.getPoiRecList()) {
                poiRecEntries.add(transformPoiEntry(info));
            }
        }
        searchRecResponse.setRecommendResult(poiRecEntries);

        //额外信息
        searchRecResponse.setExData(hotelRecResponse.getExtraDataMap());
        //策略
        searchRecResponse.putToExData("recStrategy", hotelRecResponse.getStrategy());

        return searchRecResponse;
    }

    /**
     * 解析经纬度
     * @param s
     * @return
     */
    private static Location parseLocation(String s){
        Location location = new Location(0,0);
        if (StringUtils.isNotBlank(s)){
            try {
                String[] info = s.split(Utils.SEPERATOR_COMMA);
                if (info.length == 2) {
                    location = new Location(Double.parseDouble(info[0]), Double.parseDouble(info[1]));
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return location;
    }

    /**
     * 将rec的poi信息类转化为search-rec的信息类
     * @param poiRecInfo
     * @return
     */
    private static PoiRecEntry transformPoiEntry(PoiRecInfo poiRecInfo){
        PoiRecEntry entry = new PoiRecEntry();
        entry.setDistance(poiRecInfo.getDistanceToRequest());
        entry.setPoiId(poiRecInfo.getPoiId());
        List<DealEntry> dealEntryList = new ArrayList<DealEntry>();
        if (poiRecInfo.isSetDealList()){
            List<DealRecInfo> dealRecInfoList = poiRecInfo.getDealList();
            for (DealRecInfo dealRecInfo: dealRecInfoList){
                DealEntry dealEntry = new DealEntry();
                dealEntry.setDealId(dealRecInfo.getDealId());
                dealEntry.setDealStid(dealRecInfo.getSt_id());
                dealEntryList.add(dealEntry);
            }
        }
        entry.setDeals(dealEntryList);
        entry.setPoiStid(poiRecInfo.getSt_id());
        entry.setExtensions(poiRecInfo.getExtraDataMap());
        return entry;
    }


}
