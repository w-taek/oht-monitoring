package com.example.ohtmon.scheduler;

import com.example.ohtmon.domain.SensorReading;
import com.example.ohtmon.dto.SensorDto;
import com.example.ohtmon.mapper.SensorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataReplayScheduler {

    private final SensorMapper sensorMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /** 장비별 마지막으로 Push한 sensor_reading.id */
    private final Map<String, Long> lastIdMap = new ConcurrentHashMap<>();

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
        }

        log.debug("데이터 리플레이 완료: {}대 장비", EQUIPMENT_IDS.size());
    }
}
