package com.esch.eschfindr.data;

import java.util.HashMap;

public class InternalLocation {

    private double longitude;
    private double latitude;

    public InternalLocation(){

    }

    public InternalLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public HashMap<String, Double> getRep() {
        HashMap<String, Double> data = new HashMap<>();
        data.put("longitude", longitude);
        data.put("latitude", latitude);
        return data;
    }
}
