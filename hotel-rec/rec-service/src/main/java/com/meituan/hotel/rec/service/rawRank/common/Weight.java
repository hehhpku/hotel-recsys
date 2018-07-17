package com.meituan.hotel.rec.service.rawRank.common;

/**
 * Created by hehuihui on 3/22/16
 */
public class Weight {

    private double distanceWeight;
    private double hotelTypeWeight;
    private double brandIDWeight;
    private double priceWeight;
    private double gradeWeight;

    public Weight(double distanceWeight, double hotelTypeWeight, double brandIDWeight, double priceWeight, double gradeWeight) {
        this.distanceWeight = distanceWeight;
        this.hotelTypeWeight = hotelTypeWeight;
        this.brandIDWeight = brandIDWeight;
        this.priceWeight = priceWeight;
        this.gradeWeight = gradeWeight;
    }

    public void setDistanceWeight(double distanceWeight) {
        this.distanceWeight = distanceWeight;
    }

    public void setHotelTypeWeight(double hotelTypeWeight) {
        this.hotelTypeWeight = hotelTypeWeight;
    }

    public void setBrandIDWeight(double brandIDWeight) {
        this.brandIDWeight = brandIDWeight;
    }

    public void setPriceWeight(double priceWeight) {
        this.priceWeight = priceWeight;
    }

    public void setGradeWeight(double gradeWeight) {
        this.gradeWeight = gradeWeight;
    }

    public double getDistanceWeight() {
        return distanceWeight;
    }

    public double getHotelTypeWeight() {
        return hotelTypeWeight;
    }

    public double getBrandIDWeight() {
        return brandIDWeight;
    }

    public double getPriceWeight() {
        return priceWeight;
    }

    public double getGradeWeight() {
        return gradeWeight;
    }
}
