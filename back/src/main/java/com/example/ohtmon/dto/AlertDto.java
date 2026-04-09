package com.example.ohtmon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AlertDto {

    /**
     * 알림 목록 조회 응답
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private Long id;
        private String eqId;
        private String alertLevel;
        private String sensorName;
        private Double sensorValue;
        private Double thresholdValue;
        private Boolean acknowledged;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime createdAt;
    }

    /**
     * 페이지네이션 래퍼
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private java.util.List<ListResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    /**
     * WebSocket /topic/alert Push 메시지 (CLAUDE.md 정의)
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PushMessage {
        private Long alertId;
        private String eqId;
        private String alertLevel;
        private String sensorName;
        private Double sensorValue;
        private Double thresholdValue;
        private LocalDateTime createdAt;
    }
}
