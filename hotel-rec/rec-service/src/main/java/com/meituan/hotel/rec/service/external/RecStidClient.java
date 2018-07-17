package com.meituan.hotel.rec.service.external;

import com.meituan.hotel.rec.service.utils.RecUtils;
import com.meituan.service.mobile.common.ServerHolder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Author: hehuihui,
 * hehuihui@meituan.com
 */
public class RecStidClient {
    private static Logger logger = RecUtils.getLogger(RecStidClient.class.getSimpleName());

    private HttpClient httpClient;
    private ConcurrentMap<String, String> stidMap = new ConcurrentHashMap<String, String>();
    private String stidUrl;

    public void setStidUrl(String stidUrl) {
        this.stidUrl = stidUrl;
    }

    public String register(String func, String... args) {

        String key = generateKey(func, args);
        // 从本地缓存中找
        String stid = stidMap.get(key);
        if (null != stid) {
            return String.valueOf(stid);
        }

        // 本地没有，去外部服务注册获取
        Map<String, String> map = new HashMap<String, String>();
        map.put("func", func);
        for (int i = 0; i < args.length; i += 2) {
            map.put(args[i], args[i + 1]);
        }

        try{
            StidRegisterThread stidRegisterThread = new StidRegisterThread(map);
            stidRegisterThread.start();
            stidRegisterThread.join(100);
            if (stidRegisterThread.isAlive()){
                stidRegisterThread.interrupt();
            }
            stid = stidRegisterThread.getStid();
        } catch (Exception e){
            stid = "0";
            logger.error("[ERROR] registerSTIDThreadError", e);
        }

        //合法stid, 加入缓存
        if (stid.length() > 2){
            stidMap.put(key, stid);
        }

        return String.valueOf(stid);
    }

    private String generateKey(String func, String... args) {
        StringBuilder builder = new StringBuilder(func);
        for (String arg : args) {
            builder.append(arg);
        }
        return builder.toString();
    }

    private String parseStid(String response) throws JSONException {
        JSONObject jo = new JSONObject(response);
        if (jo.has("stid"))
            return jo.getString("stid");
        else
            return "0";
    }

    class StidRegisterThread extends Thread {

        String stid = "0";
        Map<String, String> map;

        public StidRegisterThread(Map<String, String> map) {
            this.map = map;
        }

        public String getStid() {
            return stid;
        }

        @Override
        public void run() {
            HttpPost httpPost = new HttpPost(stidUrl);
            String response;
            try {
                StringEntity input = new StringEntity(new JSONObject(map).toString());
                input.setContentType(HTTP.PLAIN_TEXT_TYPE);
                input.setContentEncoding(HTTP.UTF_8);
                httpPost.setEntity(input);
                response = httpClient.execute(httpPost, new BasicResponseHandler());
                stid = parseStid(response);
            } catch (Exception e) {
                logger.error("[ERROR] registerSTIDERROR in THREAD", e);
            }
        }
    }

    public RecStidClient(){
        init();
    }

    public void init(){
        httpClient = ServerHolder.getInstance().createClientWithPool();
        //timeout 90ms
        httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT,90);
    }
}
