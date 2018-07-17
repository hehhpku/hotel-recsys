package com.meituan.hotel.rec.service.external;

import com.meituan.hotel.rec.service.common.PoiStatus;
import com.meituan.hotel.rec.service.constants.JmonitorKey;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rs.search.thrift.param.PoiRsFilterParam;
import com.meituan.hotel.rs.search.thrift.result.PoiRsFilterResult;
import com.meituan.hotel.rs.search.thrift.service.IHotelRsFilterService;
import com.meituan.jmonitor.JMonitor;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

import javax.annotation.Resource;

/**
 * Created by hehuihui on 3/21/16
 *
 */
@Service("room-status-service-client")
public class RoomStatusClient {

    @Resource
    private IHotelRsFilterService.Iface hotelRsClient;

    Logger logger = RecUtils.getLogger(RoomStatusClient.class.getSimpleName());

    /**
     * 查房态, 能保证房态查询错误时返回结果
     * @param poiIdList 待查房态的poiId
     * @param accType 是否钟点房：2为钟点房，1为全日房
     * @param checkInDay 时间戳
     * @param checkOutDay 时间戳
     * @return key=poiList, value=1:未满房，0：满房，2：未知
     */
    public Map<Integer, PoiStatus> getFullRoomPoi(List<Integer> poiIdList, int accType, int checkInDay, int checkOutDay){
        long start = System.currentTimeMillis();
        Map<Integer,PoiStatus> rsMap = new HashMap<Integer, PoiStatus>();
        List<Integer> poiIdWithFullRoom = new ArrayList<Integer>();

        //用户选择钟点房，不查房态，因为房态接口暂不支持
        if (CollectionUtils.isEmpty(poiIdList) || accType == 2)
            return rsMap;

        try{
            PoiRsFilterParam param = new PoiRsFilterParam();
            param.setPoiIdList(poiIdList);
            param.setStartDate(checkInDay);
            param.setEndDate(checkOutDay);
            param.setRoomType(accType);
            param.setInvoker(1); //筛选标记

            PoiRsFilterResult result = hotelRsClient.filterPoiRs(param);

            //查询成功则返回正确房态信息
            if (result.getStat() == 0){
                for (Map.Entry<Integer,Integer> entry:result.getResultMap().entrySet()) {
                    if (entry.getValue() == 0) {
                        rsMap.put(entry.getKey(), PoiStatus.UNAVAILABLE);
                        poiIdWithFullRoom.add(entry.getKey());
                    }
                }
            }
        } catch (Exception e){
            logger.warn(RecUtils.getWarnString("filerPoiIdViaRS"), e);
            JMonitor.add(JmonitorKey.ROOM_STATE_TIMEOUT);
        }

        long timeCost = System.currentTimeMillis() - start;
        logger.info(RecUtils.getInfoString("fullRoomPoi=" + poiIdWithFullRoom));
        JMonitor.add(JmonitorKey.ROOM_STATUS_TIME, timeCost);
        return rsMap;
    }
}
