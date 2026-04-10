package com.example.ohtmon.service;

import com.example.ohtmon.domain.SensorReading;
import com.example.ohtmon.domain.ThresholdRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEngine {

    private final ThresholdService thresholdService;
    private final AlertService alertService;

    /**
     * 센서 데이터를 임계값과 비교하여 이상 감지.
     * CAUTION 이상이면 알림 생성.
     *
     * @param data   센서 데이터
     * @param eqType 장비 유형 (OHT / AGV)
     */
    public void evaluate(SensorReading data, String eqType) {
        List<ThresholdRule> rules = thresholdService.getThresholds(eqType);
        if (rules.isEmpty()) {
            return;
        }

        // 센서명 → 실제값 매핑
        Map<String, Double> sensorValues = Map.of(
                "pm10", nullSafe(data.getPm10()),
                "pm25", nullSafe(data.getPm25()),
                "ntc_temp", nullSafe(data.getNtcTemp()),
                "ct1", nullSafe(data.getCt1()),
                "ct2", nullSafe(data.getCt2()),
                "ct3", nullSafe(data.getCt3()),
                "ct4", nullSafe(data.getCt4()),
                "ir_temp_max", nullSafe(data.getIrTempMax())
        );

        // 각 센서별 임계값 체크 → 가장 높은 등급으로 알림
        String worstLevel = null;
        String worstSensor = null;
        double worstValue = 0;
        double worstThreshold = 0;
        int worstRank = 0; // 1=CAUTION, 2=WARNING, 3=DANGER

        for (ThresholdRule rule : rules) {
            Double value = sensorValues.get(rule.getSensorName());
            if (value == null) {
                continue;
            }

            String level = null;
            double threshold = 0;
            int rank = 0;

            if (value >= rule.getDangerValue()) {
                level = "DANGER";
                threshold = rule.getDangerValue();
                rank = 3;
            } else if (value >= rule.getWarningValue()) {
                level = "WARNING";
                threshold = rule.getWarningValue();
                rank = 2;
            } else if (value >= rule.getCautionValue()) {
                level = "CAUTION";
                threshold = rule.getCautionValue();
                rank = 1;
            }

            if (level != null && rank > worstRank) {
                worstLevel = level;
                worstSensor = rule.getSensorName();
                worstValue = value;
                worstThreshold = threshold;
                worstRank = rank;
            }
        }

        if (worstLevel != null && worstRank >= 2) {
            // WARNING(2), DANGER(3)만 알림 생성 — CAUTION은 로그만
            log.warn("[ALERT] {} | {}={} > {}({})",
                    data.getEqId(), worstSensor, worstValue, worstLevel, worstThreshold);
            alertService.createAlert(data.getEqId(), worstLevel, worstSensor, worstValue, worstThreshold);
        } else if (worstLevel == null) {
            // 정상(state=0) → 억제 타이머 리셋
            alertService.resetSuppression(data.getEqId());
        }
    }

    private double nullSafe(Double value) {
        return value != null ? value : 0.0;
    }
}
