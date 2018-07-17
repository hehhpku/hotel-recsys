package com.meituan.hotel.rec.service.common;

/**
 * Created by hehuihui on 5/31/16
 */
public class Tuple {
    public int poiId;
    public long viewTimestamp;
    public int viewTimes = 0;

    public Tuple(int poiId, long viewTimestamp) {
        this.poiId = poiId;
        this.viewTimestamp = viewTimestamp;
        viewTimes = 1;
    }

    //更新最近浏览时间
    public void updateTimeStamp(long viewTimestamp){
        if (this.viewTimestamp < viewTimestamp){
            this.viewTimestamp = viewTimestamp;
        }
        viewTimes += 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple tuple = (Tuple) o;

        return poiId == tuple.poiId;

    }

    @Override
    public int hashCode() {
        return poiId;
    }
}
