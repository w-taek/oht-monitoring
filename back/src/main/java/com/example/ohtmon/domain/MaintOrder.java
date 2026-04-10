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
public class MaintOrder {

    private Long id;
    private String eqId;
    private Long alertEventId;
    private String orderType;       // PREVENTIVE, CORRECTIVE, EMERGENCY
    private String priority;        // LOW, MEDIUM, HIGH, CRITICAL
    private String status;          // OPEN, ASSIGNED, IN_PROGRESS, COMPLETED
    private String title;
    private String description;
    private String assignee;
    private String actionTaken;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
