package com.meituan.hotel.rec.cross.impl.Util;

import com.meituan.hotel.rec.cross.impl.scene.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zuolin on 15/11/23.
 */
public class GenScene {
    private Map<Integer, String> recType = new HashMap<Integer, String>();

    public GenScene() {
        recType.put(1,"hotel");
        recType.put(2,"travel");
        recType.put(3,"hotel");
        recType.put(4,"travel");
        recType.put(5,"travel");
        recType.put(6,"travel");
        recType.put(7,"travel");
        recType.put(8,"hotel");
        recType.put(9,"travel");
        recType.put(10,"hotel");
        recType.put(11,"travel");
        recType.put(12,"hotel");
        recType.put(13,"travel");
    }

    public SceneClass getScene(int scene) {
        if (scene == 1) {
            return new TrafficDepartHotelScene();
        } else if (scene == 2) {
            return new TrafficDepartTravelScene();
        } else if (scene == 3) {
            return new TrafficDestHotelScene();
        } else if (scene == 4) {
            return new TrafficDestTravelScene();
        } else if (scene == 5) {
            return new HotelDestTravelScene();
        } else if (scene == 6) {
            return new HotelGpsTravelScene();
        } else if (scene == 7) {
            return new HotelNearbyTravelScene();
        } else if (scene == 8) {
            return new TravelDestHotelScene();
        } else if (scene == 9) {
            return new TravelDestTravelScene();
        } else if (scene == 10) {
            return new TravelGpsHotelScene();
        } else if (scene == 11) {
            return new TravelGpsTravelScene();
        } else if (scene == 12) {
            return new TravelNearbyHotelScene();
        } else if (scene == 13){
            return new TravelNearbyTravelScene();
        }else{
            return null;
        }
    }

    public String getType(int scene){
        return recType.get(scene);
    }
}
