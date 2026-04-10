package com.example.ohtmon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class StatsDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SensorAvg {
        private String eqId;
        private Double pm10Avg;
        private Double pm10Max;
        private Double pm10Min;
        private Double pm25Avg;
        private Double pm25Max;
        private Double ntcTempAvg;
        private Double ntcTempMax;
        private Double ct1Avg;
        private Double ct1Max;
        private Double ct2Avg;
        private Double ct2Max;
        private Double ct3Avg;
        private Double ct3Max;
        private Double ct4Avg;
        private Double ct4Max;
        private Double irTempMaxAvg;
        private Double irTempMaxMax;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StateDistribution {
        private int state;
        private long count;
        private double ratio;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DangerRanking {
        private String eqId;
        private String eqName;
        private long totalCount;
        private long dangerCount;
        private double dangerRatio;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HealthScore {
        private String eqId;
        private String eqName;
        private double score;
        private double pm10Deviation;
        private double ntcDeviation;
        private double ctDeviation;
        private double irDeviation;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SensorTrend {
        private int hour;
        private Double avgValue;
        private Double maxValue;
    }
}
