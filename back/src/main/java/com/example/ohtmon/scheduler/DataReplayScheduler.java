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

    /** 이상감지 활성화된 장비 (jump API로 활성화, 자동 복귀 시 비활성화) */
    private final Set<String> armedSet = ConcurrentHashMap.newKeySet();

    /** 자동 복귀 예약: 경고 발생 후 다음 정상 구간으로 점프할 장비 */
    private final Map<String, Long> autoRecoveryMap = new ConcurrentHashMap<>();

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
     * 재생 위치를 특정 id로 점프 + 이상감지 활성화 (데모용)
     */
    public void jumpTo(String eqId, long targetId) {
        lastIdMap.put(eqId, targetId);
        armedSet.add(eqId);
        log.info("[REPLAY JUMP] {} → id={} (이상감지 활성화)", eqId, targetId);
    }

    /**
     * 자동 복귀 예약: 경고 1건 발생 후 recoveryId로 자동 점프
     */
    public void scheduleAutoRecovery(String eqId, long recoveryId) {
        autoRecoveryMap.put(eqId, recoveryId);
        log.info("[AUTO-RECOVERY SCHEDULED] {} → 경고 후 id={} 로 자동 복귀 예정", eqId, recoveryId);
    }

    @Scheduled(fixedRate = 2000, initialDelay = 5000)
    public void replay() {
        for (String eqId : EQUIPMENT_IDS) {
            long lastId = lastIdMap.getOrDefault(eqId, 0L);

            SensorReading data = sensorMapper.findNextReplayData(eqId, lastId);

            if (data == null) {
                lastIdMap.put(eqId, 0L);
                data = sensorMapper.findNextReplayData(eqId, 0L);
            }

            if (data == null) {
                continue;
            }

            lastIdMap.put(eqId, data.getId());

            // 1) WebSocket 센서 데이터 Push (항상)
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

            // 2) 이상감지: armed된 장비만 평가 (jump API로 활성화된 장비)
            if (armedSet.contains(eqId)) {
                String eqType = eqId.startsWith("OHT") ? "OHT" : "AGV";
                ruleEngine.evaluate(data, eqType);

                // 3) 자동 복귀: 경고 데이터 도달 → 정상 구간으로 점프 + 이상감지 비활성화
                if (data.getState() != null && data.getState() >= 2) {
                    Long recoveryId = autoRecoveryMap.remove(eqId);
                    armedSet.remove(eqId);
                    if (recoveryId != null) {
                        lastIdMap.put(eqId, recoveryId);
                        log.info("[AUTO-RECOVERY] {} → 경고 발생 후 정상 구간 id={} 로 복귀, 이상감지 비활성화", eqId, recoveryId);
                    } else {
                        log.info("[DISARMED] {} → 경고 발생, 이상감지 비활성화", eqId);
                    }
                }
            }
        }
    }
}
