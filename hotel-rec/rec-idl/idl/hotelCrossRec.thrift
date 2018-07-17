include "fb303.thrift"
namespace java com.meituan.hotel.rec.cross

enum ResponseStatus {
   OK = 0,
   ERROR = 1,
}

enum OrderTypeEnum {
    HOTEL_GROUP = 1,    #酒店团购
    HOTEL_PREPAY = 2,   #酒店预付
    TRAVEL_GROUP = 3,   #旅游团购
    TRAVEL_MTP = 4,     #MTP门票
    TRAIN = 5,          #火车票
    AIR = 6,            #飞机票
    BackPayTravel=7,    #旅游先约后买
}

enum RecSceneTypeEnum {
    TRAFFIC_DEPART_HOTEL = 1,   #交通：推荐出发地酒店
    TRAFFIC_DEPART_TRAVEL = 2,  #交通：推荐出发地旅游
    TRAFFIC_DEST_HOTEL = 3,     #交通：推荐目的地酒店
    TRAFFIC_DEST_TRAVEL = 4,    #交通：推荐目的地旅游

    HOTEL_DEST_TRAVEL = 5,      #酒店：推荐入住地旅游
    HOTEL_GPS_TRAVEL = 6,       #酒店：推荐定位地旅游
    HOTEL_NEARBY_TRAVEL = 7,    #酒店：推荐酒店周围的旅游

    TRAVEL_DEST_HOTEL = 8,      #旅游：推荐目的地的酒店
    TRAVEL_DEST_TRAVEL = 9,     #旅游：推荐目的地的旅游
    TRAVEL_GPS_HOTEL = 10,      #旅游：推荐定位地的酒店
    TRAVEL_GPS_TRAVEL = 11,     #旅游：推荐定位地的旅游
    TRAVEL_NEARBY_HOTEL = 12,   #旅游：推荐景点周围的酒店
    TRAVEL_NEARBY_TRAVEL = 13,  #旅游：推荐景点周围的旅游
}

struct PoiInfo {
  1:optional i64 poiid,
  2:optional i32 cityId, #城市ID
  3:optional double  longitude,# 经度
  4:optional double  latitude,# 纬度
}

struct CrossHotelOrderInfo {
    1:optional OrderTypeEnum type,              #单子类型：酒店团购，酒店预付
    2:optional i64 orderid,                     #本次下单的orderid
    3:optional list<PoiInfo> poiids,
    4:optional i64 dealid,
    5:optional double price,                    #单价
    6:optional i32 num,                         #房间数
    7:optional i64 orderTime,                   #下单时间
    8:optional i64 dateCheckin,                 #入店日期，酒店
    9:optional i64 dateChechout,                #离店日期，酒店
    10:optional map<string, string> extraMap    #扩展参数
}

struct CrossTravelOrderInfo {
    1:optional OrderTypeEnum type,              #单子类型：旅游团购、MTP门票
    2:optional i64 orderid,                     #本次下单的orderid
    3:optional list<PoiInfo> poiids,
    4:optional i64 dealid,
    5:optional double price,                    #单价
    6:optional i32 num,                         #房间数
    7:optional i64 orderTime,                   #下单时间
    8:optional map<string, string> extraMap     #扩展参数
}

struct CrossTicketOrderInfo {
    1:optional OrderTypeEnum type,              #单子类型：飞机票、火车票
    2:optional i64 orderid,                     #本次下单的orderid
    3:optional i64 orderTime,                   #下单时间
    4:optional i32 departureCityid,             #出发城市
    5:optional i32 destinationCityid,           #达到城市
    6:optional i64 startTime,                   #出发时间
    7:optional i64 arriveTime,                  #到达时间
    8:optional map<string, string> extraMap     #扩展参数
}

struct CrossRecRequest {
    1:i64 userId,                                       #用户id
    2:list<RecSceneTypeEnum> recSceneType,              #推荐场景类型，用于内部调用策略判断
    3:optional string strategy,                         #请求策略,默认"default"
    4:optional double userLng,                          #用户下单时的经度
    5:optional double userLat,                          #用户下单时的纬度
    6:optional i32 userOrderCityid,                     #用户下单时的城市id
    7:optional i32 userResCityId,                       #用户常住城市id
    8:optional list<CrossHotelOrderInfo> hotelOrderInfo,     #订单详细信息
    9:optional list<CrossTravelOrderInfo> travelOrderInfo,   #订单详细信息
    10:optional list<CrossTicketOrderInfo> ticketOrderInfo,  #订单详细信息
    11:optional string uuid,                            #设备信息
    12:optional string clientType,                      #客户端: iphone, android
    13:optional string appVersion,                      #版本信息: v5.8,5.9等
    14:optional list<i64> unusedTravelPoiid,            #未消费的旅游单id列表
    15:optional map<string,string> extraMap             #扩展参数
}

struct CrossRecResponse {
  1:list<i64> poiArrayList,                 #返回推荐poi的列表
  2:optional i32 totalNumOfPoi,             #返回推荐poi的个数
  3:optional string strategy,               #策略名称
  4:optional i32 status                     #执行状态
}

service HotelCrossRecService extends fb303.FacebookService {
   # 跨品类推荐
   map<string, CrossRecResponse> crossRecommend(1:CrossRecRequest request);
}