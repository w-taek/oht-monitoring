package com.example.ohtmon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class SensorDto {

    /**
     * 최신값 / 이력 조회 응답
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String eqId;
        private LocalDateTime collectedAt;
        private Double pm10;
        private Double pm25;
        private Double ntcTemp;
        private Double ct1;
        private Double ct2;
        private Double ct3;
        private Double ct4;
        private Double irTempMax;
        private Double exTemp;
        private Double exHumidity;
        private Integer state;
    }

    /**
     * 상태 요약 (대시보드용)
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StateSummary {
        private int normal;     // state=0
        private int caution;    // state=1
        private int warning;    // state=2
        private int danger;     // state=3
    }

    /**
     * 상태별 카운트 (MyBatis 매핑용)
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateCount {
        private int state;
        private int count;
    }

    /**
     * WebSocket Push 메시지 포맷 (CLAUDE.md 정의)
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RealtimeMessage {
        private String eqId;
        private Double pm10;
        private Double pm25;
        private Double ntcTemp;
        private Double ct1;
        private Double ct2;
        private Double ct3;
        private Double ct4;
        private Double irTempMax;
        private Double exTemp;
        private Double exHumidity;
        private Integer state;
        private LocalDateTime collectedAt;
    }
}
