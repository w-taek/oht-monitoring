package com.example.ohtmon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MaintOrderDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "장비 ID는 필수입니다")
        private String eqId;

        private Long alertEventId;

        @NotBlank(message = "오더 유형은 필수입니다")
        @Pattern(regexp = "PREVENTIVE|CORRECTIVE|EMERGENCY", message = "유효하지 않은 오더 유형입니다")
        private String orderType;

        @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "유효하지 않은 우선순위입니다")
        private String priority;

        @NotBlank(message = "제목은 필수입니다")
        private String title;

        private String description;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusUpdateRequest {

        @NotBlank(message = "상태값은 필수입니다")
        @Pattern(regexp = "OPEN|ASSIGNED|IN_PROGRESS|COMPLETED", message = "유효하지 않은 상태값입니다")
        private String status;

        private String assignee;
        private String actionTaken;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private Long id;
        private String eqId;
        private String eqName;
        private String orderType;
        private String priority;
        private String status;
        private String title;
        private String assignee;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailResponse {
        private Long id;
        private String eqId;
        private String eqName;
        private Long alertEventId;
        private String alertLevel;
        private String alertSensorName;
        private String orderType;
        private String priority;
        private String status;
        private String title;
        private String description;
        private String assignee;
        private String actionTaken;
        private LocalDateTime createdAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<ListResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
