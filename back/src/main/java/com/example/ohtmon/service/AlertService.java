package com.example.ohtmon.service;

import com.example.ohtmon.domain.AlertEvent;
import com.example.ohtmon.dto.AlertDto;
import com.example.ohtmon.mapper.AlertMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertMapper alertMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 알림 생성 + WebSocket Push
     */
    public void createAlert(String eqId, String alertLevel, String sensorName,
                            double sensorValue, double thresholdValue) {
        AlertEvent event = AlertEvent.builder()
                .eqId(eqId)
                .alertLevel(alertLevel)
                .sensorName(sensorName)
                .sensorValue(sensorValue)
                .thresholdValue(thresholdValue)
                .build();

        alertMapper.insert(event);

        // WebSocket /topic/alert Push (CLAUDE.md 포맷)
        AlertDto.PushMessage pushMessage = AlertDto.PushMessage.builder()
                .alertId(event.getId())
                .eqId(eqId)
                .alertLevel(alertLevel)
                .sensorName(sensorName)
                .sensorValue(sensorValue)
                .thresholdValue(thresholdValue)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/alert", pushMessage);
        log.debug("알림 Push 전송: {} {} {} = {}", eqId, alertLevel, sensorName, sensorValue);
    }

    /**
     * 알림 목록 조회 (페이지네이션)
     */
    public AlertDto.PageResponse getAlerts(String eqId, String level, int page, int size) {
        int offset = page * size;
        List<AlertDto.ListResponse> content = alertMapper.findAll(eqId, level, offset, size);
        long totalElements = alertMapper.countAll(eqId, level);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return AlertDto.PageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    /**
     * 알림 확인 처리
     */
    public void acknowledge(long id) {
        alertMapper.acknowledge(id);
    }
}
