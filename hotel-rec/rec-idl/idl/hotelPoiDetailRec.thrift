include "fb303.thrift"

namespace java com.meituan.hotel.rec.poidetail

enum RecResponseStatus{
    OK = 200,                                       # 正常返回
    FORBIDDEN = 403,                                # 拒绝响应
    UNCONNECT = 404, 								# 无法链接
    ERROR = 500,                                    # 出错直接返回
    DEGRADE = 501                                   # 降级直接返回
}

enum RecRequestType{
 	REC_NEAR_HOT_POI = 0,
 	REC_BRAND_POI = 1,
 	REC_PREORDER_POI = 2,
 	REC_VACATION_POI = 3
}

# 请求的poi信息
struct DetailPoiInfo{
 	1: required i32 poiId,
 	2: optional list<i32> dealIdList,
    3: optional list<i32> goodsIdList,
    4: optional map<string, string> extraMap   # 额外信息kv
}

#响应的poi信息
struct PoiDetailResp{
 	1: required i32 poiId,
 	2: optional double distanceToPoi,          # poi与user正在浏览的poi的距离
 	3: optional double distanceToUser,         # poi与user定位坐标的距离, -1代表计算距离错误（因缺少用户经纬度信息）
 	4: optional map<string, string> extraMap,  # 额外信息kv
 	5: optional string stid                    # 报表及日志需要的stid
}

#请求的user信息
struct DetailUserInfo{
 1: required string uuid,
 2: optional i64 userId = -1,
 3: optional string accommodationType = "DR",   # 住宿类型：钟点房（HR），全日房（DR）
 4: optional double userLat,                    # user的定位纬度
 5: optional double userLng,                    # user的定位经度
 6: optional i32 dateCheckIn,                   # 入住时间: yyyyMMdd格式
 7: optional i32 dateCheckOut,                  # 退房时间
 8: optional string clientType,                 # 客户端: android, iphone, wap, www, 等
 9: optional string appVersion,                 # 客户端版本号:6.0等形式
 10: optional i32 userCityId,                   # 用户定位城市
 11: optional i32 appCityId,                    # 用户在平台选择的城市，ci字段
 12: optional i32 channelCityId,                # 用户在频道内选择的城市
 13: optional i64 actionTime,                   # 用户操作时间：毫秒
 14: optional map<string, string> extraMap      # 额外信息kv
}

#poi详情页推荐的请求
struct PoiDetailRecRequest{
 1: required DetailPoiInfo detailPoiInfo,       # poi信息
 2: required DetailUserInfo detailUserInfo,     # user信息
 3: required RecRequestType requestType,    	# 请求推荐的类型
 4: optional string strategy,                   # 策略
 5: optional map<string, string> extraMap       # 额外信息kv
}

#poi详情页推荐的响应
struct PoiDetailRecResponse{
 1: required list<PoiDetailResp> poiRecList,    # 推荐列表（有序）
 2: required RecResponseStatus status,          # 响应状态
 3: optional i64 sessionId,                     # 唯一标识本次请求的id
 4: optional string strategy,                   # 推荐策略
 5: optional map<string, string> extraMap       # 额外信息kv
}

#服务接口
service HotelPoiDetaiRecService extends fb303.FacebookService{
 PoiDetailRecResponse poiDetailRecommend(1: PoiDetailRecRequest request);
}