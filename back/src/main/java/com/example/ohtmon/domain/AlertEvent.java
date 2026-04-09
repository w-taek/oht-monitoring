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
public class AlertEvent {

    private Long id;
    private String eqId;
    private String alertLevel;      // CAUTION, WARNING, DANGER
    private String sensorName;
    private Double sensorValue;
    private Double thresholdValue;
    private Boolean acknowledged;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime createdAt;
}
