package com.example.ohtmon.service;

import com.example.ohtmon.domain.AlertEvent;
import com.example.ohtmon.dto.AlertDto;
import com.example.ohtmon.mapper.AlertMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertMapper alertMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /** 장비별 마지막 알림 발생 시각 (중복 억제용) */
    private final Map<String, Instant> lastAlertTimeMap = new ConcurrentHashMap<>();

    /** 억제 시간: 3분 */
    private static final long SUPPRESSION_SECONDS = 180;

    /**
     * 알림 생성 + WebSocket Push (중복 억제 적용)
     * 같은 eq_id는 레벨 무관하게 3분간 알림 1개만 발생
     *
     * @return true = 알림 생성됨, false = 억제됨
     */
    public boolean createAlert(String eqId, String alertLevel, String sensorName,
                               double sensorValue, double thresholdValue) {
        // 중복 억제 체크
        Instant lastTime = lastAlertTimeMap.get(eqId);
        if (lastTime != null && Instant.now().isBefore(lastTime.plusSeconds(SUPPRESSION_SECONDS))) {
            log.debug("[SUPPRESSED] {} | 3분 내 중복 알림 억제", eqId);
            return false;
        }

        AlertEvent event = AlertEvent.builder()
                .eqId(eqId)
                .alertLevel(alertLevel)
                .sensorName(sensorName)
                .sensorValue(sensorValue)
                .thresholdValue(thresholdValue)
                .build();

        alertMapper.insert(event);

        // 억제 타이머 기록
        lastAlertTimeMap.put(eqId, Instant.now());

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
        log.info("[ALERT] {} {} {} = {} (threshold={})", eqId, alertLevel, sensorName, sensorValue, thresholdValue);
        return true;
    }

    /**
     * state가 정상(0)으로 복귀 시 억제 타이머 리셋
     */
    public void resetSuppression(String eqId) {
        if (lastAlertTimeMap.remove(eqId) != null) {
            log.debug("[SUPPRESSION RESET] {} 정상 복귀 → 억제 해제", eqId);
        }
    }

    /**
     * 특정 장비의 억제 타이머 강제 초기화 (jump API용)
     */
    public void clearSuppression(String eqId) {
        lastAlertTimeMap.remove(eqId);
        log.info("[SUPPRESSION CLEAR] {} 억제 타이머 초기화", eqId);
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
