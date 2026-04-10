package com.example.ohtmon.service;

import com.example.ohtmon.domain.MaintOrder;
import com.example.ohtmon.dto.MaintOrderDto;
import com.example.ohtmon.mapper.EquipmentMapper;
import com.example.ohtmon.mapper.MaintOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintOrderService {

    private final MaintOrderMapper maintOrderMapper;
    private final EquipmentMapper equipmentMapper;

    /** 허용되는 상태 전이 */
    private static final Map<String, String> VALID_TRANSITIONS = Map.of(
            "OPEN", "ASSIGNED",
            "ASSIGNED", "IN_PROGRESS",
            "IN_PROGRESS", "COMPLETED"
    );

    public MaintOrderDto.PageResponse getOrders(String eqId, String status, int page, int size) {
        int offset = page * size;
        List<MaintOrderDto.ListResponse> content = maintOrderMapper.findAll(eqId, status, offset, size);
        long totalElements = maintOrderMapper.countAll(eqId, status);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return MaintOrderDto.PageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    public MaintOrderDto.DetailResponse getOrder(long id) {
        MaintOrderDto.DetailResponse order = maintOrderMapper.findById(id);
        if (order == null) {
            throw new IllegalArgumentException("정비 오더를 찾을 수 없습니다: " + id);
        }
        return order;
    }

    public MaintOrder createOrder(MaintOrderDto.CreateRequest request) {
        // 장비 존재 확인
        if (equipmentMapper.findById(request.getEqId()) == null) {
            throw new IllegalArgumentException("장비를 찾을 수 없습니다: " + request.getEqId());
        }

        MaintOrder order = MaintOrder.builder()
                .eqId(request.getEqId())
                .alertEventId(request.getAlertEventId())
                .orderType(request.getOrderType())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        maintOrderMapper.insert(order);
        log.info("정비 오더 생성: id={}, eqId={}, type={}", order.getId(), order.getEqId(), order.getOrderType());
        return order;
    }

    public void updateStatus(long id, MaintOrderDto.StatusUpdateRequest request) {
        MaintOrderDto.DetailResponse current = getOrder(id);
        String currentStatus = current.getStatus();
        String newStatus = request.getStatus();

        // 상태 전이 검증
        String allowedNext = VALID_TRANSITIONS.get(currentStatus);
        if (allowedNext == null || !allowedNext.equals(newStatus)) {
            throw new IllegalStateException(
                    String.format("잘못된 상태 전이입니다: %s → %s (허용: %s → %s)",
                            currentStatus, newStatus, currentStatus,
                            allowedNext != null ? allowedNext : "없음"));
        }

        String assignee = null;
        String actionTaken = null;
        LocalDateTime startedAt = null;
        LocalDateTime completedAt = null;

        switch (newStatus) {
            case "ASSIGNED" -> {
                if (request.getAssignee() == null || request.getAssignee().isBlank()) {
                    throw new IllegalStateException("ASSIGNED 전이 시 담당자(assignee)는 필수입니다");
                }
                assignee = request.getAssignee();
            }
            case "IN_PROGRESS" -> {
                startedAt = LocalDateTime.now();
            }
            case "COMPLETED" -> {
                if (request.getActionTaken() == null || request.getActionTaken().isBlank()) {
                    throw new IllegalStateException("COMPLETED 전이 시 조치 내용(actionTaken)은 필수입니다");
                }
                actionTaken = request.getActionTaken();
                completedAt = LocalDateTime.now();
            }
        }

        maintOrderMapper.updateStatus(id, newStatus, assignee, actionTaken, startedAt, completedAt);
        log.info("정비 오더 상태 변경: id={}, {} → {}", id, currentStatus, newStatus);

        // COMPLETED 시 장비 상태를 RUNNING으로 자동 복귀
        if ("COMPLETED".equals(newStatus)) {
            equipmentMapper.updateStatus(current.getEqId(), "RUNNING");
            log.info("장비 상태 자동 복귀: {} → RUNNING", current.getEqId());
        }
    }
}
