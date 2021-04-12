package com.mrkiriss.imwifi.model;

import com.mrkiriss.imwifi.entity.AccessPoint;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CalibrationLocationPoint {
    private double lat;
    private double lon;
    private List<List<AccessPoint>> calibrationSets;
}
