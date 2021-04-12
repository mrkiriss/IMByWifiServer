package com.mrkiriss.imwifi.model;

import lombok.Data;

@Data
public class CalibrationAccessPoint {
    private String mac;
    private Integer rssiSum=0;
    private Integer numberOfRssiAdditions =0;

    public CalibrationAccessPoint(String mac){
        this.mac=mac;
    }

    public void addToRssiSum(Integer value){
        rssiSum+=value;
        numberOfRssiAdditions++;
    }
}
