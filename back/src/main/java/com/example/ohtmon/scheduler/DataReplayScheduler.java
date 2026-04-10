package com.example.ohtmon.scheduler;

import com.example.ohtmon.domain.SensorReading;
import com.example.ohtmon.dto.SensorDto;
import com.example.ohtmon.mapper.SensorMapper;
import com.example.ohtmon.service.RuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataReplayScheduler {

    private final SensorMapper sensorMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final RuleEngine ruleEngine;

    /** 장비별 마지막으로 Push한 sensor_reading.id */
    private final Map<String, Long> lastIdMap = new ConcurrentHashMap<>();

    /** 자동 복귀 예약: 경고 발생 후 다음 정상 구간으로 점프할 장비 */
    private final Map<String, Long> autoRecoveryMap = new ConcurrentHashMap<>();

    /** 자동 복귀 대기 중인 장비 (경고 1건 발생 대기) */
    private final Set<String> pendingAlertSet = ConcurrentHashMap.newKeySet();

    private static final List<String> EQUIPMENT_IDS;

    static {
        EQUIPMENT_IDS = new java.util.ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            EQUIPMENT_IDS.add(String.format("OHT-%02d", i));
        }
        for (int i = 1; i <= 20; i++) {
            EQUIPMENT_IDS.add(String.format("AGV-%02d", i));
        }
    }

    /**
     * 재생 위치를 특정 id로 점프 (데모용)
     */
    public void jumpTo(String eqId, long targetId) {
        lastIdMap.put(eqId, targetId);
        log.info("[REPLAY JUMP] {} → id={}", eqId, targetId);
    }

    /**
     * 자동 복귀 예약: 경고 1건 발생 후 recoveryId로 자동 점프
     */
    public void scheduleAutoRecovery(String eqId, long recoveryId) {
        autoRecoveryMap.put(eqId, recoveryId);
        pendingAlertSet.add(eqId);
        log.info("[AUTO-RECOVERY SCHEDULED] {} → 경고 후 id={} 로 자동 복귀 예정", eqId, recoveryId);
    }

    @Scheduled(fixedRate = 2000)
    public void replay() {
        for (String eqId : EQUIPMENT_IDS) {
            long lastId = lastIdMap.getOrDefault(eqId, 0L);

            SensorReading data = sensorMapper.findNextReplayData(eqId, lastId);

            if (data == null) {
                // 끝에 도달 → 처음부터 다시 (circular)
                lastIdMap.put(eqId, 0L);
                data = sensorMapper.findNextReplayData(eqId, 0L);
            }

            if (data == null) {
                continue; // 데이터 없는 장비는 스킵
            }

            lastIdMap.put(eqId, data.getId());

            // 1) WebSocket 센서 데이터 Push
            SensorDto.RealtimeMessage message = SensorDto.RealtimeMessage.builder()
                    .eqId(data.getEqId())
                    .pm10(data.getPm10())
                    .pm25(data.getPm25())
                    .ntcTemp(data.getNtcTemp())
                    .ct1(data.getCt1())
                    .ct2(data.getCt2())
                    .ct3(data.getCt3())
                    .ct4(data.getCt4())
                    .irTempMax(data.getIrTempMax())
                    .exTemp(data.getExTemp())
                    .exHumidity(data.getExHumidity())
                    .state(data.getState())
                    .collectedAt(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/sensor/" + eqId, message);

            // 2) 이상감지: eqId 접두사로 장비유형 판별
            String eqType = eqId.startsWith("OHT") ? "OHT" : "AGV";
            ruleEngine.evaluate(data, eqType);

            // 3) 자동 복귀 체크: 경고 state 데이터가 나오면 → 알림 1건 발생 완료 → 정상 구간으로 점프
            if (pendingAlertSet.contains(eqId) && data.getState() != null && data.getState() >= 2) {
                Long recoveryId = autoRecoveryMap.remove(eqId);
                pendingAlertSet.remove(eqId);
                if (recoveryId != null) {
                    lastIdMap.put(eqId, recoveryId);
                    log.info("[AUTO-RECOVERY] {} → 경고 발생 후 정상 구간 id={} 로 자동 복귀", eqId, recoveryId);
                }
            }
        }

        log.debug("데이터 리플레이 완료: {}대 장비", EQUIPMENT_IDS.size());
    }
}
