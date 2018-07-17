package com.meituan.hotel.rec.service.external;

/**
 * QueryLocation.java
 *
 * @author jiangweisen@meituan.com
 * @date 2015-09-18
 * @brief
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.hotel.rec.thrift.Location;
import com.meituan.jmonitor.JMonitor;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;


public class QueryLocationService {

    // 百度地图API
    private static final String BAIDU_API_PLACE = "http://api.map.baidu.com/place/v2/search?output=json";
    // 百度地图鉴权Key（因调用频次限制，需使用多个key）
    private static final String[] BAIDU_API_KEY = {"syHkFUv2hiqlK9vanxX48wzz", "1k2DBGoC9M6vGmRyV3ku0FxH",
            "KU3H1wOIcChYiPEfMkuGUuvV", "aibgqZjgs4sNOryUgCYS90Cx"};

    // 设置超时时间
    private static final int URL_CONNECT_TIMEOUT = 300;
    private static final int URL_READ_TIMEOUT = 300;

    private static final Logger logger = RecUtils.getLogger(QueryLocationService.class.getSimpleName());

    // 发送http请求，获取数据
    public static String sendHttpRequest(String url)
    {
        StringBuilder response = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            URLConnection connection = urlObject.openConnection();
            connection.setConnectTimeout(URL_CONNECT_TIMEOUT);
            connection.setReadTimeout(URL_READ_TIMEOUT);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ( (line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
        } catch (Exception e) {
            logger.error("sendHttpRequest() error:" + e + ";url=" + url);
        }

        //logger.info("url=" + url);
        return response.toString();
    }

    // 组装百度地图api
    public static String getBaiduMapApi(String query, String city)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(BAIDU_API_PLACE);

        try {
            query = URLEncoder.encode(query, "utf-8");
        } catch (Exception e) {
            logger.error("URLencode query failed. query=" + query + ";msg=" + e);
        }

        sb.append("&query=" + query);
        sb.append("&region=" + city);

        Random random = new Random();
        int index = random.nextInt(BAIDU_API_KEY.length);
        sb.append("&ak=" + BAIDU_API_KEY[index]);

        return sb.toString();
    }

    public static Location parseJsonLocation(String json) {
        try {
            JSONObject jo = JSONObject.parseObject(json);

            int status = jo.getIntValue("status");
            if (status != 0) {
                return null;
            }

            JSONArray ja = jo.getJSONArray("results");
            if (ja.size() == 0) {
                return null;
            }

            JSONObject firstItem = ja.getJSONObject(0);
            if (!firstItem.containsKey("location")) {
                return null;
            }

            double lat = firstItem.getJSONObject("location").getDouble("lat");
            double lng = firstItem.getJSONObject("location").getDouble("lng");

            return new Location(lat, lng);
        } catch (Exception e) {
            logger.warn(RecUtils.getWarnString("parseJsonLocation"), e);
        }
        return null;
    }

    public static Location getLocationByQuery(String query, String city) {
        long startTime = System.currentTimeMillis();

        String url = getBaiduMapApi(query, city);
        String json = sendHttpRequest(url);
        Location point = parseJsonLocation(json);

        long costTime = (System.currentTimeMillis() - startTime);
        JMonitor.add("search.recommend.getLocationByQuery.time", costTime);

        return point;
    }
}
