include "fb303.thrift"

namespace java com.meituan.hotel.search.recommend

enum PoiSearchSource {
  DEFAULT = 0,
  HOTEL = 1,
  MERGESERVER = 2,
}

enum PoiResponseStatus {
  OK = 0,
  ERROR = 1,
}

struct RerankStidInfo {
  1: required string strategy,
  2: optional string alg,
  3: optional string smz001,
  4: optional map<string, string> kv,      // 存储其他stid注册参数
  5: optional map<string, string> ext,     // 存储扩展参数，如globalid
}

struct DealEntry {
  1: required i32 dealId,
  2: required bool hidden = false,         // 是否隐藏
  3: optional string dealStid = "",
}

struct PoiRecEntry {
  1: required i32 poiId = -1,
  2: optional string poiStid = "",
  3: optional list<DealEntry> deals,
  4: optional double distance = -1.0,
  5: optional map<string, string> extensions,
}

struct SearchRecRequest {
  1: required string query = '',
  2: required string city = '',               // "北京“
  5: optional i32 offset = 0,
  6: optional i32 limit = 1000,
  7: optional string orderby = '',            // "sales:desc"
  8: optional string location = '',           // "lat,lng"
  9: optional i32 distance,
  10: optional map<string, string> filters,
  11: optional string uuid = '',
  12: optional string cateId = '',
  13: optional string areaId = '',
  14: optional bool newCateSystem = false,

  15: optional PoiSearchSource source = PoiSearchSource.DEFAULT, // 标识搜索来源
  16: optional i64 beginTime = 0,
  17: optional i64 endTime = 0,

  30: optional map<string, string> extensions,
}

struct SearchRecResponse {
  1: required PoiResponseStatus status,
  2: optional list<PoiRecEntry> poiResult,             // 搜索结果
  3: optional list<PoiRecEntry> recommendResult,       // 推荐结果
  4: optional string stid,                          // 列表stid
  5: optional string location,                      // 地标
  6: optional i32 totalCount,                       // 结果总数
  10: optional string redirectUri,                  // 重定向URI
  11: optional string switchCity,                   // 跳转城市
  12: optional string extentions,                   // 旅游相关信息

  20: optional RerankStidInfo stidInfo,

  30: optional map<string, string> exData,
}

service HotelSearchRecommendService extends fb303.FacebookService {

    SearchRecResponse searchRecommend(1:SearchRecRequest request);
}