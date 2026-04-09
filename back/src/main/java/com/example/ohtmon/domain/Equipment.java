package com.example.ohtmon.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {

    private String eqId;
    private String eqName;
    private String eqType;          // OHT, AGV
    private String manufacturer;
    private String location;
    private LocalDate installDate;
    private String status;          // RUNNING, STOPPED, MAINTENANCE
    private LocalDateTime createdAt;
}
