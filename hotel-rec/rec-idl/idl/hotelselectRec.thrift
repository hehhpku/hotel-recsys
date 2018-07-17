include "fb303.thrift"

namespace java com.meituan.hotel.rec.select

struct SelectRecRequest {
  1:optional string strategy,                #请求策略
  2:i32 cityId,                             #城市id
  3:optional i32 roomType,                  #房型 0:不限，1：大床房 2：单人房 3：双床房
  4:optional i32 accommodationType,         #入住类型 0:不限，1：全日房 2：钟点房
  5:optional list<i32> hotelType,           #酒店类型,使用的是酒店子品类cate
  6:optional list<i32> brandId,             #品牌id ，使用官方品牌库
  7:optional double lng,                    #经度
  8:optional double lat,                    #纬度
  9:optional double priceLow,               #价格下限
  10:optional double priceHigh,             #价格上限
  11:optional string uuid,                  #设备信息
  12:optional string sortingMethod,         #排序方式
  13:optional string dateCheckin,           #入店日期
  14:optional string dateChechout,          #离店日期
  15:optional string clientType,            #客户端: iphone, android
  16:optional string appVersion,            #版本信息: v5.8,5.9等
  17:optional i32 locationType,             #用户请求的地点:0:商圈,1:地标,2:附近,3:地铁全线,4:全城
  18:optional map<string,string> extraMap,   #扩展参数
  19:optional list<i32> roomTypeList,       #房型 0:不限，1：大床房 2：单人房 3：双床房, 4:三人间，5:床位
  20:optional i32 businessType,             #是否要求预订: 0:不需要支持预订，1:支持预订
  21:optional i32 receiptProvided          #是否有发票要求：0:不需要支持发票, 1:支持发票
}

struct SelectRecResponse {
  1:list<i32> poiArrayList,                 #返回推荐poi的列表
  2:optional i32 totalNumOfPoi,             #返回推荐poi的个数
  3:optional string strategy,               #策略名称
}

service HotelSelectRecService extends fb303.FacebookService {

    SelectRecResponse selectRecommend(1:SelectRecRequest request);
}