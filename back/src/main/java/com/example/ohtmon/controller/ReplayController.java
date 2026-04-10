package com.example.ohtmon.controller;

import com.example.ohtmon.common.ApiResponse;
import com.example.ohtmon.domain.SensorReading;
import com.example.ohtmon.mapper.SensorMapper;
import com.example.ohtmon.scheduler.DataReplayScheduler;
import com.example.ohtmon.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/replay")
@RequiredArgsConstructor
public class ReplayController {

    private final DataReplayScheduler replayScheduler;
    private final SensorMapper sensorMapper;
    private final AlertService alertService;

    /**
     * 데모용: 특정 장비의 재생 위치를 경고/위험 발생 직전으로 점프
     * 경고 1건 발생 후 자동으로 정상 데이터 구간으로 복귀
     */
    @PostMapping("/jump")
    public ResponseEntity<ApiResponse<Map<String, Object>>> jump(
            @RequestParam(defaultValue = "OHT-01") String eqId,
            @RequestParam(defaultValue = "WARNING") String level) {

        int minState = switch (level.toUpperCase()) {
            case "DANGER" -> 3;
            default -> 2;  // WARNING
        };

        // 1) 해당 state 이상인 첫 번째 row 탐색
        SensorReading target = sensorMapper.findFirstByState(eqId, minState);
        if (target == null) {
            throw new IllegalArgumentException(
                    String.format("장비 %s에 state>=%d 인 데이터가 없습니다", eqId, minState));
        }

        // 2) 억제 타이머 초기화 → 경고가 반드시 발생하도록
        alertService.clearSuppression(eqId);

        // 3) 5칸 앞으로 점프 → 약 10초 후 경고 발생
        long jumpToId = Math.max(0, target.getId() - 5);
        replayScheduler.jumpTo(eqId, jumpToId);

        // 4) 자동 복귀 예약: 경고 row 이후 첫 번째 정상(state=0) row로 복귀
        SensorReading normalRow = sensorMapper.findNextNormalAfter(eqId, target.getId());
        if (normalRow != null) {
            replayScheduler.scheduleAutoRecovery(eqId, normalRow.getId());
        }

        log.info("[REPLAY JUMP] {} level={} → jumpTo={}, alert@={}, recovery@={}",
                eqId, level, jumpToId, target.getId(),
                normalRow != null ? normalRow.getId() : "N/A");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("eqId", eqId);
        result.put("jumpedToId", jumpToId);
        result.put("alertTargetId", target.getId());
        result.put("expectedAlertIn", "약 10초");
        result.put("autoRecovery", normalRow != null);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
