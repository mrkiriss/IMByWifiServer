package com.mrkiriss.imwifi.services;

import com.mrkiriss.imwifi.entity.AccessPoint;
import com.mrkiriss.imwifi.entity.LocationPoint;
import com.mrkiriss.imwifi.model.CalibrationAccessPoint;
import com.mrkiriss.imwifi.model.CalibrationLocationPoint;
import com.mrkiriss.imwifi.model.DefinedLocationPoint;
import com.mrkiriss.imwifi.model.DeltaLocationPoint;
import com.mrkiriss.imwifi.repositories.AccessPointRepository;
import com.mrkiriss.imwifi.repositories.LocationPointRepository;
import com.sun.istack.NotNull;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IMDataService {

    @Autowired
    LocationPointRepository locationPointRepository;
    @Autowired
    AccessPointRepository accessPointRepository;

    @NotNull
    private double bigDelta;
    private final double avoidingDivisionByZero=0.00001;

    public DefinedLocationPoint defineLocationPoint(CalibrationLocationPoint calibrationLocationPoint){
        LocationPoint smoothedLocationPoint = smoothPointOfCalibration(calibrationLocationPoint);
        if (smoothedLocationPoint.getAccessPoints().size()==0) return null;
        List<LocationPoint> possibleLocations = chooseAllSuitableLocationPoints(smoothedLocationPoint.collectMACs());
        List<DeltaLocationPoint> deltaLocationPoints = generateDeltaLocationPoints(smoothedLocationPoint, possibleLocations);
        return calculateLocationPoint(deltaLocationPoints);
    }
    // Выбор точек из базы с количеством совподений в MACs с набором от клиента >2
    private List<LocationPoint> chooseAllSuitableLocationPoints(List<String> currentMacsCollection){
        //locationPointRepository.findAllSuitableByMacCount(smoothedLocationPoint.collectMACs())
        System.out.println("--Выбор точек из базы с количеством совподений в MACs с набором от клиента >2--");

        List<LocationPoint> result = new ArrayList<>();
        List<String> possiblesMacsCollection;

        System.out.println("Текущий набор MAC\n"+currentMacsCollection.toString());

        for (LocationPoint possibleLP: locationPointRepository.findAll()){
            possiblesMacsCollection=possibleLP.collectMACs();
            System.out.println("Воможный набор до\n"+possiblesMacsCollection.toString());

            possiblesMacsCollection.retainAll(currentMacsCollection);
            System.out.println("Воможный набор после\n"+possiblesMacsCollection.toString());

            if (possiblesMacsCollection.size()>3) result.add(possibleLP);
        }

        return result;
    }
    private List<DeltaLocationPoint> generateDeltaLocationPoints(LocationPoint currentLocation, List<LocationPoint> possibleLocations){
        final double minRssi = -90;
        double inverseSumOfDelta=0;
        final double avoidingDivisionByZero=0.00001;
        double delta;

        List<DeltaLocationPoint> result = new ArrayList<>();

        for (LocationPoint possibleLocation : possibleLocations){
            DeltaLocationPoint deltaLocationPoint = new DeltaLocationPoint(possibleLocation.getLat(), possibleLocation.getLon());
            double sum=0;

            for (AccessPoint accessPoint: currentLocation.getAccessPoints()){
                AccessPoint foundAP = possibleLocation.findAPbyMAC(accessPoint.getMac());
                if (foundAP!=null){
                    sum+=Math.pow(foundAP.getRssi()-accessPoint.getRssi(), 2);
                }else{
                    sum+=Math.pow(minRssi, 2);
                }
            }
            delta=sum/currentLocation.getAccessPoints().size();
            inverseSumOfDelta+=1/(delta+avoidingDivisionByZero);
            deltaLocationPoint.setDelta(delta);
            result.add(deltaLocationPoint);
        }

        this.bigDelta=inverseSumOfDelta;
        return result;
    }
    private DefinedLocationPoint calculateLocationPoint(List<DeltaLocationPoint> deltaLocationPoints){

        DefinedLocationPoint result = new DefinedLocationPoint();
        double sumLat=0;
        double sumLon=0;

        for (DeltaLocationPoint deltaLocationPoint:deltaLocationPoints){
            sumLat+=1/(deltaLocationPoint.getDelta()+avoidingDivisionByZero)*deltaLocationPoint.getLat();
            sumLon+=1/(deltaLocationPoint.getDelta()+avoidingDivisionByZero)*deltaLocationPoint.getLon();
        }

        result.setLat(sumLat/bigDelta);
        result.setLon(sumLon/bigDelta);

        return result;
    }

    public LocationPoint savePointToBase(CalibrationLocationPoint calibrationLocationPoint){
        LocationPoint result = smoothPointOfCalibration(calibrationLocationPoint);
        System.out.println("Location point saved with data: "+result.toString());
        locationPointRepository.save(result);
        return result;
    }

    private LocationPoint smoothPointOfCalibration(CalibrationLocationPoint calibrationLocationPoint){
        List<CalibrationAccessPoint> listOfCalibrationAccessPoints= generateListOfCalibrationAPs(calibrationLocationPoint);
        LocationPoint result = new LocationPoint();
        result.setLat(calibrationLocationPoint.getLat());
        result.setLon(calibrationLocationPoint.getLon());
        result.setAccessPoints(selectSuitableAPs(listOfCalibrationAccessPoints, calibrationLocationPoint.getCalibrationSets().size()));
        return result;
    }

    // возвращает список объектов, в каждом из которых mac и сумма всех встреченных на этот mac rssi и количество этих rssi
    private List<CalibrationAccessPoint> generateListOfCalibrationAPs(CalibrationLocationPoint calibrationLocationPoint){
        List<CalibrationAccessPoint> result = new ArrayList<>();
        CalibrationAccessPoint currentCalibrationAP;

        for (List<AccessPoint> accessPointList : calibrationLocationPoint.getCalibrationSets()){
            for (AccessPoint accessPoint : accessPointList){

                currentCalibrationAP=findCalibrationAccessPoint(accessPoint.getMac(), result);

                if (currentCalibrationAP==null){
                    currentCalibrationAP=new CalibrationAccessPoint(accessPoint.getMac());
                    result.add(currentCalibrationAP);
                }

                currentCalibrationAP.addToRssiSum(accessPoint.getRssi());
            }
        }

        return result;
    }
    private CalibrationAccessPoint findCalibrationAccessPoint(String checkedMac, List<CalibrationAccessPoint> calibrationAccessPoints){
        for (CalibrationAccessPoint calibrationAP : calibrationAccessPoints){
            if (calibrationAP.getMac().equals(checkedMac)) return calibrationAP;
        }
        return null;
    }
    // возвращает список AP с усреднёнными rssi, только те, количество которых по mac превышает половину во всех тренировочных наборах
    private List<AccessPoint> selectSuitableAPs(List<CalibrationAccessPoint> calibrationAccessPointList, int numberOfCalibrationKits){

        final int thresholdForNumberOfAPs = numberOfCalibrationKits/2;
        int averageRssi;
        List<AccessPoint> result = new ArrayList<>();

        for (CalibrationAccessPoint calibrationAccessPoint : calibrationAccessPointList){
            if (calibrationAccessPoint.getNumberOfRssiAdditions()>thresholdForNumberOfAPs){
                averageRssi=calibrationAccessPoint.getRssiSum()/calibrationAccessPoint.getNumberOfRssiAdditions();
                result.add(new AccessPoint(calibrationAccessPoint.getMac(), averageRssi));
            }
        }

        return result;
    }

    public void clearServer(){
        locationPointRepository.deleteAll();
    }
}
