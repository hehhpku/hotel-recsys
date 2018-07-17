package com.meituan.hotel.rec.service.external;

import com.google.common.collect.Maps;

import com.meituan.mobile.service.recommend.city.CityInfo;

import org.apache.commons.collections.MapUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiangweisen and hehuihui on 3/16/16
 */
public class CityService {
    private static volatile CityService instance = null;
    private static volatile long lastUpdateTime = 0;

    private Map<String, CityInfo> cityMap = Maps.newHashMap();
    private Map<Integer, CityInfo> cityIdMap = Maps.newHashMap();

    private static final long EXPIRE_TIME = 3600000 * 12;
    private static final String CITY_URL = "http://open.vip.sankuai.com/api/city/query?id=all";
    private static final Logger logger = LoggerFactory.getLogger(CityService.class);

    private CityService() {
        updateCityMap();
    }

    public static boolean isExpired() {
        long now = System.currentTimeMillis();
        return now - lastUpdateTime > EXPIRE_TIME;
    }

    public static CityService getInstance() {
        if (instance == null || isExpired()) {
            synchronized (CityService.class) {
                if (instance == null || isExpired()) {
                    lastUpdateTime = System.currentTimeMillis();
                    instance = new CityService();
                }
            }
        }
        return instance;
    }

    private List<CityInfo> listCities() {
        try {
            List<CityInfo> cities = new ArrayList<CityInfo>();
            URL url = new URL(CITY_URL);
            URLConnection connection = url.openConnection();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONObject obj = new JSONObject(sb.toString());
            JSONArray dataArray = obj.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject each = dataArray.getJSONObject(i);
                CityInfo city = new CityInfo();
                city.setId(each.getInt("id"));
                city.setName(each.getString("name"));
                city.setLocationId(each.getString("locationid"));
                city.setAcronym(each.getString("acronym"));
                city.setPinyin(each.getString("pinyin"));
                city.setRank(each.getString("rank"));
                city.setOnlineTime(each.getLong("onlineTime"));
                cities.add(city);
            }
            return cities;
        } catch (Exception e) {
            logger.error("listCities() error:" + e);
            return Collections.emptyList();
        }
    }

    private boolean updateCityMap() {
        long now = System.currentTimeMillis();

        try {
            Map<String, CityInfo> newCityMap = new HashMap<String, CityInfo>();
            Map<Integer, CityInfo> newCityIdMap = new HashMap<Integer, CityInfo>();
            List<CityInfo> cities = listCities();
            for (CityInfo c : cities) {
                newCityMap.put(c.getName(), c);
                newCityIdMap.put(c.getId(), c);
            }

            // 容错：当能正常取到数据时，才进行更新
            if (newCityMap.size() > cityMap.size() / 2) {
                cityMap = newCityMap;
                cityIdMap = newCityIdMap;
            }
        } catch (Exception e) {
            logger.error("updateCityMap() error:" + e);
            return false;
        }
        long end = System.currentTimeMillis();
        logger.info("updateCityMap() success: timeCost=" + (end - now) + ";cityMap=" + cityMap.keySet());

        return true;
    }

    public int getIdByName(String cityName) {
        int cityId = -1;
        CityInfo info = cityMap.get(cityName);
        if (info != null) {
            cityId = info.getId();
        }
        return cityId;
    }

    public String getRankById(int id) {
        CityInfo info = cityIdMap.get(id);
        if (info != null) {
            return info.getRank();
        }
        return null;
    }

    public String getNameById(int id) {
        CityInfo info = cityIdMap.get(id);
        if (info != null) {
            return info.getName();
        }
        return null;
    }

    public Map<String, CityInfo> getCityMap() {
        return cityMap;
    }

    public boolean isCityName(String name) {
        if (!MapUtils.isEmpty(cityMap)) {
            return cityMap.containsKey(name);
        }
        return false;
    }

}
