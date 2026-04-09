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
public class ThresholdRule {

    private Integer id;
    private String eqType;          // OHT, AGV
    private String sensorName;      // pm10, pm25, ntc_temp, ct1~ct4, ir_temp_max
    private Double cautionValue;
    private Double warningValue;
    private Double dangerValue;
    private LocalDateTime updatedAt;
}
