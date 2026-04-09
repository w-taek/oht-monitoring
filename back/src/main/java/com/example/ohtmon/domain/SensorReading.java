package com.example.ohtmon.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorReading {

    private Long id;
    private String eqId;
    private LocalDateTime collectedAt;
    private Double pm10;
    private Double pm25;
    private Double pm10Val;
    private Double ntcTemp;
    private Double ct1;
    private Double ct2;
    private Double ct3;
    private Double ct4;
    private Double irTempMax;
    private Integer irX;
    private Integer irY;
    private Double exTemp;
    private Double exHumidity;
    private Double exLux;
    private Integer state;
    private Integer cumulativeOperatingDay;
    private Integer equipmentHistory;
}
