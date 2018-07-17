package com.meituan.hotel.rec.service.utils;

/**
 * SearchUtil.java
 *
 * @author jiangweisen@meituan.com
 * @date 2015-09-18
 * @brief
 */


import com.meituan.hotel.rec.service.common.RecRequest;
import com.meituan.hotel.rec.service.external.CityService;
import com.meituan.hotel.rec.service.external.QueryLocationService;
import com.meituan.hotel.rec.service.external.SinaiPoiClient;
import com.meituan.hotel.rec.service.rawRank.PoiFeatureGetter;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.mobile.sinai.base.common.PoiFields;
import com.meituan.service.mobile.loc2city.service.Loc2CityService;
import com.meituan.service.mobile.sinai.client.model.PoiModel;

import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

@Service
public class SearchUtil {
    private static final Logger logger = RecUtils.getLogger(SearchUtil.class.getSimpleName());


    private static final List<String> locList = Arrays.asList("路", "道", "街", "店", "区");

    @Resource
    private SinaiPoiClient sinaiPoiClient;

    private static Map<String, Integer> stopwordMap = new HashMap<String, Integer>();
    private static Map<String, String> synonymMap = new HashMap<String, String>();
    private static Map<String, String> rewriteCityMap = new HashMap<String, String>();

    private static final Map<String,String> sceneIdNameMap;


    static  {
        loadStopwordMap(RecUtils.getResourceFile("data/stopword.dict"));
        loadSynonym(RecUtils.getResourceFile("data/synonym.dict"));
        loadRewriteCityMap(RecUtils.getResourceFile("data/rewrite_city"));
        UserDefineLibrary.loadFile(UserDefineLibrary.FOREST, new File(RecUtils.getResourceFile("data/hotel_brand.dict")));
        sceneIdNameMap = loadSceneIdMap();
    }

    /**
     * 保持 与原搜索一致， TODO debug转化率用，后期可删除
     * @return
     */
    private static Map<String,String> loadSceneIdMap(){
        Map<String,String> map = new HashMap<String, String>();
        map.put("0", "one");
        map.put("1", "zero");
        map.put("2", "all");
        map.put("3", "multi");
        return map;
    }

    /**
     * 查询srcList在dest字符串中，第一次出现的位置
     * @param srcList
     * @param dest
     * @return
     */
    public static int findFirstPosition(List<String> srcList, String dest) {
        int pos = -1;
        for (String src : srcList) {
            pos = dest.indexOf(src);
            if (pos > 0) {
                break;
            }
        }
        return pos;
    }

    /**
     * 酒店停用词表（例如：酒店、宾馆）
     * @param file
     * @return
     */
    public static Map<String, Integer> loadStopwordMap(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;

            while ((line = br.readLine()) != null) {
                stopwordMap.put(line, 1);
            }
        } catch (Exception e) {
            logger.error(RecUtils.getErrorString("loadStopwordMap"), e);
        }
        return stopwordMap;
    }

    /**
     * 酒店同义品牌字典（例如：七天 => 7天）
     * @param file
     * @return
     */
    public static Map<String, String> loadSynonym(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens.length != 2) {
                    continue;
                }
                synonymMap.put(tokens[0], tokens[1]);
            }
        } catch (Exception e) {
            logger.error(RecUtils.getErrorString("loadSynonym"), e);
        }
        return synonymMap;
    }

    /**
     * 酒店停用词表
     * @return
     */
    public static Map<String, Integer> getStopwordMap() {
        return stopwordMap;
    }

    /**
     * 酒店同义品牌
     * @return
     */
    public static Map<String, String> getSynonymMap() {
        return synonymMap;
    }

    public static Location transferBd2Tx(double lat, double lng) {
        return new Location(lat - 0.006, lng - 0.006475);
    }

    /**
     * 城市改写字典
     * @param file
     */
    public static void loadRewriteCityMap(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens.length != 2) {
                    continue;
                }
                String originCity  = tokens[0];
                String rewriteCity = tokens[1];

                rewriteCityMap.put(originCity, rewriteCity);
            }
        } catch (Exception e) {
            logger.error(RecUtils.getErrorString("loadRewriteCityMap"), e);
        }
    }

    public static Map<String, String> getRewriteCityMap() {
        return rewriteCityMap;
    }

    /**
     * 从query中识别出，用户的真实目标城市
     * @param city 请求城市
     * @param query 请求query
     * @return 真实目标城市
     */
    public static String getTargetCity(String city, String query) {
        // 分词
        List<Term> segList = ToAnalysis.parse(query);

        String realCity = city;
        for (int i = 0; i < segList.size(); i++) {
            Term term = segList.get(i);
            String name = term.getName();
            name = name.replace("市", "");

            // 如果query中出现了同名城市，则判定为同城
            if (name.equals(city)) {
                break;
            }

            // 如果此单词为城市名
            if (CityService.getInstance().isCityName(name)) {
                int pos = SearchUtil.findFirstPosition(SearchUtil.locList, query);

                // 判断其是否为道路、小区名（例如：南昌市北京南路）
                if (pos > 0 && term.getOffe() + name.length() + 1 >= pos) {
                    continue;
                } else {
                    realCity = name;
                    break;
                }
            }
        }

        return realCity;
    }

    /**
     * 根据搜索结果个数，改写sceneId: 0 = 只有一个结果且合作, 1 = 搜索无结果, 2 = 全是未合作商家, 3 = 搜索少结果且带有合作商家
     * @param originSceneId
     * @param searchResultSize
     * @return
     */
    public static int rewriteSceneId(int originSceneId, int searchResultSize){
        int sceneId = originSceneId;
        if (originSceneId != 2){
            switch (searchResultSize){
                case 0:
                    sceneId = 1;
                    break;
                case 1:
                    sceneId = 0;
                    break;
                default:
                    sceneId = 3;
                    break;
            }
        } else if (searchResultSize == 1) {
           sceneId = 0;
        }

        return sceneId;
    }

    /**
     * 获取搜索推荐用户的住宿位置
     * @param query
     * @param cityName
     * @param poiIds
     * @param userLocation
     * @return
     */
    public Location getLocationFromQueryOrSearchResult(RecRequest request, String query, String cityName,
                                          List<Integer> poiIds, Location userLocation, int channelCItyId, Map<String, String> infoMap){
        Location location;
        //搜索无结果推荐，调用百度地图
        if (CollectionUtils.isEmpty(poiIds)){
            location = QueryLocationService.getLocationByQuery(query, cityName);
            if (location != null) {
                location = transferBd2Tx(location.getLattitude(), location.getLongitude());
                request.setBaiduLocation(location);
                return location;
            } else {
                return null;
            }
        }

        //有结果，找住宿地点
        List<PoiModel> poiModelList = sinaiPoiClient.getPoiModelFromSinai(poiIds, PoiFeatureGetter.FIELDS);
        Map<Integer, PoiModel> poiModelMap = new HashMap<Integer, PoiModel>();
        for (PoiModel poiModel: poiModelList){
            poiModelMap.put(poiModel.getId(), poiModel);
        }
        int firstPoiId = poiIds.get(0);
        //sinai无数据返回，返回用户位置
        if (MapUtils.isEmpty(poiModelMap))
            return userLocation;

        //单结果，返回该poi位置
        PoiModel poiModel = poiModelMap.get(firstPoiId);
        double firstPoiPrice = poiModel.getLowestprice();
        infoMap.put("firstPoiPrice",String.valueOf(firstPoiPrice));
        Location firstPoiLocation = new Location(poiModel.getLatitude(), poiModel.getLongitude());
        if (poiIds.size() == 1){
            return firstPoiLocation;
        }

        //多结果，聚类判断中心店
        int iterations = 0;
        double totalDistance = 0;
        // 计算前3个POI两两之间的距离
        int minSize = Math.min(3, poiIds.size());
        for (int i = 0; i < minSize; i++) {
            for ( int j = i + 1; j < minSize; j++) {
                PoiModel poiA = poiModelMap.get(poiIds.get(i));
                PoiModel poiB = poiModelMap.get(poiIds.get(j));
                double distance = RecDistanceUtils.calDistance(poiA.getLongitude(),
                        poiA.getLatitude(),
                        poiB.getLongitude(), poiB.getLatitude());
                totalDistance += distance;
                iterations++;
            }
        }
        // 若平均距离<1km，则取第一个POI作为中心点
        if (totalDistance < 1500 * iterations) {
            return firstPoiLocation;
        }

        //本异地找位置
        if (RecDistanceUtils.isValidLocation(userLocation)){
            int userLocationCityId = Loc2CityService.getInstance().loc2city(
                    (float)userLocation.getLattitude(), (float)userLocation.getLongitude());
            if (userLocationCityId > 0 && channelCItyId == userLocationCityId){
                double minDistance = Integer.MAX_VALUE;
                PoiModel nearestPoiModel = null;
                for (Integer poiId : poiIds) {
                    PoiModel pm = poiModelMap.get(poiId);
                    double distance = RecDistanceUtils.calDistance(userLocation.getLongitude(), userLocation.getLattitude(),
                            pm.getLongitude(), pm.getLatitude());
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestPoiModel = pm;
                    }
                }
                if (nearestPoiModel != null) {
                    return new Location(nearestPoiModel.getLatitude(), nearestPoiModel.getLongitude());
                }
            }
        }
        return firstPoiLocation;
    }

    /**
     * 根据场景Id返回场景名称，TODO debug
     * @param id
     * @return
     */
    public static String getSceneNameById(String id){
        if (sceneIdNameMap.containsKey(id)){
            return sceneIdNameMap.get(id);
        } else {
            return "unknown_scene";
        }
    }
}
