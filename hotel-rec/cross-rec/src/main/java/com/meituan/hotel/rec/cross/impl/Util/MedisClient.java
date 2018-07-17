package com.meituan.hotel.rec.cross.impl.Util;

import com.meituan.cache.redisCluster.client.MedisFactory;
import com.meituan.cache.redisCluster.client.typeInterface.IMedis;

/**
 * MedisClient.java
 *
 * @author zuolin02@meituan.com
 * @date 2015-10-28
 * @brief
 */


public class MedisClient {
    private IMedis imedis;
    private static MedisClient instance;

    private MedisClient(){
        imedis = MedisFactory.getInstance("applicationContext.xml", "medisClient");
    }

    public static MedisClient getMedisClient(){
        if(instance == null){
            instance = new MedisClient();
        }
        return instance;
    }
    public IMedis getInstance() {
        return imedis;
    }
}
