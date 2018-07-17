include "fb303.thrift"
namespace java com.meituan.hotel.rec.thrift

enum RecServiceStatus{
    OK = 200,                                       # 正常返回
    FORBIDDEN = 403,                                # 拒绝响应
    UNCONNECT = 404, 								# 无法链接
    ERROR = 500,                                    # 出错直接返回
    DEGRADE = 501                                   # 降级直接返回
}

enum RecServiceType{
 	SELECT_REC = 0,         #筛选推荐
 	SEARCH_REC = 1,         #搜索推荐
 	POI_DETAIL_REC = 2,     #poi详情页推荐
 	CROSS_REC = 3,          #交叉推荐
 	REC_PREORDER_POI = 4,   #酒店退款详情页推荐
 	REC_VACATION_POI = 5    #度假页猜你喜欢推荐
}

enum AccommodationType{
    DR = 0,            #全日房
    HR = 1,            #钟点房
    OTH = 2            #其他
}

enum SortingMethod {
    SMART = 0,
    DISTANCE = 1,
    SOLD = 2,
    AVG_SCORE = 3,
    LOWEST_PRICE = 4,
    HIGH_PRICE = 5
}

struct Location{
    1: required double lattitude = 0.0,  #纬度
    2: required double longitude = 0.0   #经度
}

struct DealRecInfo{
    1: required i32 dealId,
    2: optional string ct_poi = "",
    3: optional string st_id = "",
    4: optional map<string, string> extraDataMap
}

struct PoiRecInfo{
    1: required i32 poiId,
    2: optional list<DealRecInfo> dealList,        #deal信息列表
    3: optional list<DealRecInfo> goodList,        #good信息列表
    4: optional string ct_poi = "",                #ct_poi信息
    5: optional string st_id = "",                 #st_id信息
    6: optional double distanceToUser = -1.0,      #与用户定位的距离
    7: optional double distanceToRequest = -1.0,   #与用户请求住宿的位置的距离
    8: optional Location poiLocation,              #poi的位置
    9: optional map<string, string> extraDataMap
}

struct UserRecInfo{
    1: required string uuid = "",
    2: optional i64 userId = -1,
    3: optional Location userLocation,                             #用户定位的经纬度
    4: optional i32 userLocationCityId = 0,                        #用户的定位城市
    5: optional i32 appCityId = 0,                                 #客户端选择的城市，ci字段
    6: optional i32 channelCityId = 0,                             #用户频道内选择的城市
    7: optional i32 checkInDate,                                   #入住时间yyyyMMdd格式
    8: optional i32 checkOutDate,                                  #离店时间
    9: optional string clientType = "NULL",                        #客户端类型，iphone, android, www, wap, other, NULL
    10: optional string appVersion = "0",                          #客户端版本号
    11: optional AccommodationType accType = AccommodationType.DR, #用户的住宿类型（钟点房or全日房）
    12: optional map<string, string> extraDataMap
}

struct SelectRecExtraMsg{
    1: optional list<i32> roomTypeList,                     #房型0:不限，1：大床房 2：单人房 3：双床房, 4:三人间，5:床位
    2: optional list<i32> hotelType,                        #酒店类型,使用的是酒店子品类cate
    3: optional list<i32> brandId,                          #品牌id ，使用官方品牌库
    4: optional double priceLow = -1.0,                     #用户筛选的价格区间
    5: optional double priceHigh = -1.0,
    6: optional Location requestLocation,                   #用户请求的住宿地点（筛选地标的经纬度）
    7: optional i32 locationType,                           #位置类型0:商圈,1:地标,2:附近,3:地铁全线,4:全城
    8: optional i32 businessType,                           #是否支持预订:0:不需要支持预订，1:支持预订
    9: optional i32 receiptProvided,                        #是否要发票:0:不需要支持发票, 1:支持发票
    10: optional map<string, string> extraDataMap

}

struct SearchRecExtraMsg{
    1: required string query = "",                     #搜索query
    2: required string cityName = "",                  #城市名称
    3: optional i32 sceneId,                           #场景id
    4: optional map<string, string> extraDataMap,
    5: optional Location identifiedLocation,           #qs根据query识别出来的位置
    6: optional i32 intentionType                      #qs识别出query的意图，例如：地标，学校，品牌等
}

struct PoiDetailRecExtraMsg{
    1: optional map<string, string> extraDataMap
}

struct CrossRecExtraMsg{
    1: optional map<string, string> extraDataMap
}

struct HotelRecRequest{
    1: required RecServiceType serviceType,             #请求哪个推荐
    2: optional i32 offset = 0,                         #偏移量
    3: optional SortingMethod sortingMethod,            #排序方法
    4: optional string strategy,                        #策略方法
    5: optional list<i32> poiOnShow,                    #已有poi列表,例如筛选已有结果，搜索已有结果，poi详情页的poiId
    6: optional SelectRecExtraMsg selectRecMsg,
    7: optional SearchRecExtraMsg searchRecMsg,
    8: optional PoiDetailRecExtraMsg poiDetailRecMsg,
    9: optional CrossRecExtraMsg crossRecMsg,
    10: optional map<string, string> extraDataMap,
    11: optional UserRecInfo userInfo
}

struct HotelRecResponse{
    1: required RecServiceStatus serviceStatus,         #返回状态
    2: required list<PoiRecInfo> PoiRecList,            #推荐结果列表
    3: optional i32 recNum,                             #推荐个数
    4: optional string strategy,                        #策略
    5: optional string algorithm,                       #算法
    6: optional map<string, string> extraDataMap
}

service HotelRecService extends fb303.FacebookService{
    HotelRecResponse recommend(1: HotelRecRequest hotelRecRequest);
}