package com.mrkiriss.imwifi.controllers;

import com.mrkiriss.imwifi.entity.LocationPoint;
import com.mrkiriss.imwifi.model.CalibrationLocationPoint;
import com.mrkiriss.imwifi.model.DefinedLocationPoint;
import com.mrkiriss.imwifi.model.StringResponse;
import com.mrkiriss.imwifi.services.IMDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/location")
public class IndoorMappingDataController {

    @Autowired
    private IMDataService imDataService;

    @PostMapping("/define")
    public ResponseEntity<?> getLocationPoint(@RequestBody CalibrationLocationPoint calibrationLocationPoint){
        try {
            System.out.println("Запрос на определение местоположения");
            DefinedLocationPoint resultPoint = imDataService.defineLocationPoint(calibrationLocationPoint);
            if (resultPoint==null){
                System.out.println("Местоположение не оределено - пустой результат");
                return ResponseEntity.notFound().build();
            }
            System.out.println("Местоположение оределено успешно");
            return ResponseEntity.ok(resultPoint);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addPoint(@RequestBody CalibrationLocationPoint calibrationLocationPoint){
        try {
            System.out.println("Запрос на добавление точки");
            LocationPoint locationPoint = imDataService.savePointToBase(calibrationLocationPoint);
            System.out.println("Запрос обработан успешно\n");
            StringResponse response = new StringResponse();
            response.setResponse(locationPoint.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> clearServer(){
        try {
            System.out.println("Запрос на очистку сервера");
            imDataService.clearServer();
            System.out.println("Запрос обработан успешно");
            return ResponseEntity.ok("Good");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
