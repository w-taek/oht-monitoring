package com.example.ohtmon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EquipmentDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "장비 ID는 필수입니다")
        private String eqId;

        @NotBlank(message = "장비명은 필수입니다")
        private String eqName;

        @NotBlank(message = "장비 유형은 필수입니다")
        @Pattern(regexp = "OHT|AGV", message = "장비 유형은 OHT 또는 AGV만 가능합니다")
        private String eqType;

        private String manufacturer;
        private String location;
        private LocalDate installDate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusUpdateRequest {

        @NotBlank(message = "상태값은 필수입니다")
        @Pattern(regexp = "RUNNING|STOPPED|MAINTENANCE", message = "유효하지 않은 상태값입니다")
        private String status;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private String eqId;
        private String eqName;
        private String eqType;
        private String manufacturer;
        private String location;
        private String status;
        private LocalDate installDate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailResponse {
        private String eqId;
        private String eqName;
        private String eqType;
        private String manufacturer;
        private String location;
        private String status;
        private LocalDate installDate;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusCount {
        private String status;
        private int count;
    }
}
