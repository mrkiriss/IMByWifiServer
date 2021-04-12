package com.mrkiriss.imwifi.entity;

import lombok.Data;

import javax.persistence.*;
import javax.persistence.JoinColumn;

@Entity
@Data
public class AccessPoint {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String mac;
    private Integer rssi;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private LocationPoint lp;

    public AccessPoint(String mac, Integer rssi){
        this.mac=mac;
        this.rssi=rssi;
    }
    public AccessPoint(){};
}
