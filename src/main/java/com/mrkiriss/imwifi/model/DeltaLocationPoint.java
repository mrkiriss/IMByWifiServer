package com.mrkiriss.imwifi.model;

import lombok.Data;

@Data
public class DeltaLocationPoint {
    private Double lat;
    private Double lon;
    private Double delta;

    public DeltaLocationPoint(Double lat, Double lon){
        this.lat=lat;
        this.lon=lon;
    }
}
