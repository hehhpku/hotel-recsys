package com.meituan.hotel.rec.service.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.meituan.hotel.rec.common.utils.RecMapUtils;
import com.meituan.hotel.rec.data.*;
import com.meituan.hotel.rec.data.client.RecDataClient;
import com.meituan.hotel.rec.service.common.*;
import com.meituan.hotel.rec.service.constants.*;
import com.meituan.hotel.rec.service.external.*;
import com.meituan.hotel.rec.service.utils.*;
import com.meituan.hotel.rec.thrift.AccommodationType;
import com.meituan.hotel.rec.thrift.RecServiceType;
import com.meituan.jmonitor.JMonitor;
import com.meituan.mobile.sinai.base.common.PoiFields;
import com.meituan.service.mobile.sinai.client.model.PoiModel;
import com.meituan.service.mobile.util.CollectionUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 3/21/16
 */
@Service("filter-service")
public class Filter {

    public static final  Logger logger = RecUtils.getLogger(Filter.class.getSimpleName());

    @Resource(name = "room-status-service-client")
    private RoomStatusClient roomStatusClient;

    @Resource(name = "sinai-poi-client")
    private SinaiPoiClient sinaiPoiClient;

    @Resource
    private RecDataClient recDataClient;

    /**
     * 过滤满房，未合作，请求钟点房时没有钟点房的酒店
     * @param request
     * @param poiEntryList
     * @param joLog
     * @return
     */
    public List<PoiEntry> filter(RecRequest request, List<PoiEntry> poiEntryList, JSONObject joLog){
        long start = System.currentTimeMillis();
        if (CollectionUtils.isEmpty(poiEntryList))
            return poiEntryList;

        List<Integer> poiIdList = TransformationUtils.getPoiIdListFromPoiEntry(poiEntryList);

        //房态
        int accType = transferAccType(request.getAccommodationType());
        int checkInDate = request.getCheckInDate();
        int checkOutDate = request.getCheckOutDate();
        Map<Integer, PoiStatus> poiRSMap = new HashedMap<Integer, PoiStatus>();
        if (AccommodationType.DR == request.getAccommodationType()){
            poiRSMap = roomStatusClient.getFullRoomPoi(poiIdList, accType, checkInDate, checkOutDate);
        }
        //不合作，请求钟点房时无钟点房
        boolean isFilterAccType = (request.getAccommodationType() == AccommodationType.HR);
        List<String> fields = Arrays.asList(
                PoiFields.lowestPrice,
                PoiFields.isHourRoom,
                PoiFields.brandId,
                PoiFields.dIds,
                PoiFields.zlSourceType,
                PoiFields.sourceType,
                PoiFields.cityIds
        );
        //过滤只有团购的poi, 预订异常页推荐只能出有预订的poi
        boolean isFilterPreorder = false;
        if (RecServiceType.REC_PREORDER_POI == request.getServiceType()){
            isFilterPreorder = true;
        }
        Map<Integer, PoiStatus> poiCoopMap = filterViaSinai(poiIdList, fields, isFilterAccType, isFilterPreorder);

        //过滤满房及未合作商家，poi维度过滤
        Map<Integer, PoiStatus> poiStatusMap = new HashMap<Integer, PoiStatus>();

        RecMapUtils.putALl(poiStatusMap, poiRSMap);
        RecMapUtils.putALl(poiStatusMap, poiCoopMap);
        logger.info(RecUtils.getInfoString("unavailableMap=" + poiStatusMap.toString()));
        List<PoiEntry> poiEntryListFiltered = new ArrayList<PoiEntry>();
        for (PoiEntry poiEntry: poiEntryList){
            int poiId = poiEntry.getPoiId();
            if (poiStatusMap.containsKey(poiId) && poiStatusMap.get(poiId) == PoiStatus.UNAVAILABLE){
                continue;
            }
            poiEntryListFiltered.add(poiEntry);
        }

        //搜索需求：全日房时，过滤poi下钟点房deal;钟点房时，过滤poi下的全日房deal
        if (request.getServiceType() == RecServiceType.SEARCH_REC
                || request.getServiceType() ==RecServiceType.SELECT_REC) {
            poiEntryListFiltered = filterDealByRoomType(poiEntryListFiltered, request.getAccommodationType());
        }

        long timeCost = System.currentTimeMillis() - start;
        JMonitor.add(JmonitorKey.FILTER_TIME, timeCost);
        return poiEntryListFiltered;
    }

    /**
     * 利用sinai过滤poi
     * @param poiIdList 待过滤的poi列表
     * @param fields
     * @param isFilterHourRoom 是否要过滤没有钟点房的poi, true = 要过滤
     * @param isFilterPreorder 是否要过滤只有团购的poi, true = 要过滤
     * @return
     */
    private Map<Integer, PoiStatus> filterViaSinai(List<Integer> poiIdList, List<String> fields, boolean isFilterHourRoom, boolean isFilterPreorder){
        Map<Integer, PoiStatus> map = new HashMap<Integer, PoiStatus>();
        List<PoiModel> poiModelList = sinaiPoiClient.getPoiModelFromSinai(poiIdList, fields);
        for (PoiModel poiModel: poiModelList){
            int poiId = poiModel.getId();
            //根据要求过滤钟点房和已下线poi
            if (( isFilterHourRoom && !poiModel.getHotel().isHourRoom() )
                    || isOfflinePoi(poiModel) ){
                map.put(poiId, PoiStatus.UNAVAILABLE);
            }

            //判断是否要过滤只有团购的poi
            if (isFilterPreorder
                    && poiModel.getHotel().getZlSourceType() <= 0
                    && poiModel.getHotel().getSourceType() <= 0){
                map.put(poiId, PoiStatus.UNAVAILABLE);
            }
            // 判断是否含冥王星cityId
            if (poiModel.getCityIds().contains(8000)) {
                map.put(poiId, PoiStatus.UNAVAILABLE);
            }
        }
        return map;
    }

    /**
     * 将住宿类型转化成房态接口要求的
     * @param accType
     * @return
     */
    private int transferAccType(AccommodationType accType){
        if (AccommodationType.HR == accType)
            return 2;
        else
            return 1;
    }

    /**
     * 搜索推荐中过滤酒店：钟点房时不出全日房deal, 全日房时不出钟点房deal
     * @param poiEntryList
     * @param accType
     * @return
     */
    public List<PoiEntry> filterDealByRoomType(List<PoiEntry> poiEntryList, AccommodationType accType) {
        boolean isSearchDayRoom = AccommodationType.DR == accType;

        // 组装dealId列表
        List<Integer> dealIds = Lists.newArrayList();
        for (PoiEntry poiEntry : poiEntryList) {
            if (CollectionUtils.isNotEmpty(poiEntry.getDealIdList())) {
                dealIds.addAll(poiEntry.getDealIdList());
            }
        }

        // 查询deal的基本信息
        Map<Integer, HotelDealBasicInfo> dealInfoMap = this.getDealInfoMap(dealIds);

        // 遍历deal信息，执行过滤
        List<PoiEntry> result = Lists.newArrayList();
        for (PoiEntry poiEntry : poiEntryList) {
            //要求钟点房且没有团购deal则不添加这个poi，因为直连和预付没有钟点房，只有团购才有钟点房产品
            if (CollectionUtils.isEmpty(poiEntry.getDealIdList()) && !isSearchDayRoom){
                continue;
            }

            // 有deal则根据住宿类型要求过滤deal
            // 即使没有团购deal,但要求全日房，仍应该添加这个poi，因为该poi是预付或直连
            List<Integer> dealIdList = new ArrayList<Integer>();
            if (CollectionUtils.isNotEmpty(poiEntry.getDealIdList())) {
                for (int dealId : poiEntry.getDealIdList()) {
                    HotelDealBasicInfo dealInfo = dealInfoMap.get(dealId);
                    if (dealInfo != null && dealId > 0) {
                        // 搜全日房，过滤钟点房deal
                        if (isSearchDayRoom && Constants.HOUR_ROOM.equals(dealInfo.getAccType())) {
                            continue;
                        }
                        // 搜钟点房，过滤全日房deal
                        if (!isSearchDayRoom && Constants.DAY_ROOM.equals(dealInfo.getAccType())) {
                            continue;
                        }
                    }
                    dealIdList.add(dealId);
                }
            }

            poiEntry.setDealIdList(dealIdList);
            result.add(poiEntry);
        }

        return result;
    }

    public Map<Integer, HotelDealBasicInfo> getDealInfoMap(List<Integer> dealIds) {
        DealInfoReq hotelDealInfoReq = new DealInfoReq();
        hotelDealInfoReq.setDealIdList(dealIds);

        RecDataRequest request = new RecDataRequest();
        request.setOption(RequestOption.GET_HOTEL_DEAL);
        request.setDealInfoReq(hotelDealInfoReq);
        RecDataResponse response = recDataClient.getHotelDealInfo(request);

        Map<Integer, HotelDealBasicInfo> dealInfoMap = Maps.newHashMap();
        if (response.getStatus() == ResponseStatus.OK) {
            List<HotelDealBasicInfo> dealInfoList = response.getDealInfoList();
            if (CollectionUtil.isNotEmpty(dealInfoList)) {
                for (HotelDealBasicInfo dealInfo : dealInfoList) {
                    dealInfoMap.put(dealInfo.getDealId(), dealInfo);
                }
            }
        }

        return dealInfoMap;
    }

    /**
     * 判断poi是否下线
     * @param poiModel
     * @return
     */
    public static boolean isOfflinePoi(PoiModel poiModel){
        return poiModel.getLowestprice() <= 0.01
                || ( StringUtils.isBlank(poiModel.getdIds())
                && poiModel.getHotel().getZlSourceType() <= 0
                && poiModel.getHotel().getSourceType() <= 0 );
    }
}
