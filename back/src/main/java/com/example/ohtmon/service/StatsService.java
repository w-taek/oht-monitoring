package com.example.ohtmon.service;

import com.example.ohtmon.domain.ThresholdRule;
import com.example.ohtmon.dto.StatsDto;
import com.example.ohtmon.mapper.StatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsMapper statsMapper;
    private final ThresholdService thresholdService;

    /** 허용되는 센서 컬럼명 (SQL Injection 방지) */
    private static final Set<String> ALLOWED_SENSORS = Set.of(
            "pm10", "pm25", "ntc_temp", "ct1", "ct2", "ct3", "ct4", "ir_temp_max"
    );

    public List<StatsDto.SensorAvg> getSensorAvg(String eqId) {
        return statsMapper.sensorAvgByEquipment(eqId);
    }

    public List<StatsDto.StateDistribution> getStateDistribution() {
        return statsMapper.stateDistribution();
    }

    public List<StatsDto.DangerRanking> getDangerRanking(int limit) {
        return statsMapper.dangerRanking(limit > 0 ? limit : 10);
    }

    /**
     * 건강 점수 계산.
     * OHT 장비에는 OHT 임계값, AGV 장비에는 AGV 임계값 적용.
     * eqId가 없으면 OHT 임계값 기준으로 전체 장비 조회 (단순화).
     */
    public List<StatsDto.HealthScore> getHealthScore(String eqId) {
        // 장비 유형 판별 → 적합한 임계값 사용
        String eqType = (eqId != null && eqId.startsWith("AGV")) ? "AGV" : "OHT";
        List<ThresholdRule> rules = thresholdService.getThresholds(eqType);

        double pm10Caution = getCautionValue(rules, "pm10", 80);
        double ntcCaution = getCautionValue(rules, "ntc_temp", 40);
        double ctCaution = getCautionValue(rules, "ct1", 3);
        double irCaution = getCautionValue(rules, "ir_temp_max", 45);

        return statsMapper.healthScoreRaw(eqId, pm10Caution, ntcCaution, ctCaution, irCaution);
    }

    public List<StatsDto.SensorTrend> getSensorTrend(String eqId, String sensorName) {
        if (!ALLOWED_SENSORS.contains(sensorName)) {
            throw new IllegalArgumentException("유효하지 않은 센서명입니다: " + sensorName);
        }
        return statsMapper.sensorTrend(eqId, sensorName);
    }

    private double getCautionValue(List<ThresholdRule> rules, String sensorName, double defaultValue) {
        return rules.stream()
                .filter(r -> r.getSensorName().equals(sensorName))
                .findFirst()
                .map(ThresholdRule::getCautionValue)
                .orElse(defaultValue);
    }
}
