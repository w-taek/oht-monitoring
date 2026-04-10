package com.example.ohtmon.service;

import com.example.ohtmon.domain.ThresholdRule;
import com.example.ohtmon.mapper.ThresholdMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThresholdService {

    private final ThresholdMapper thresholdMapper;

    /** 캐시: eqType → List<ThresholdRule> */
    private final Map<String, List<ThresholdRule>> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadCache() {
        List<ThresholdRule> all = thresholdMapper.findAll();
        Map<String, List<ThresholdRule>> grouped = all.stream()
                .collect(Collectors.groupingBy(ThresholdRule::getEqType));
        cache.clear();
        cache.putAll(grouped);
        log.info("임계값 캐시 로딩 완료: OHT {}건, AGV {}건",
                cache.getOrDefault("OHT", Collections.emptyList()).size(),
                cache.getOrDefault("AGV", Collections.emptyList()).size());
    }

    public List<ThresholdRule> getThresholds(String eqType) {
        return cache.getOrDefault(eqType, Collections.emptyList());
    }

    public List<ThresholdRule> getAllThresholds() {
        return thresholdMapper.findAll();
    }

    public void updateThreshold(int id, double cautionValue, double warningValue, double dangerValue) {
        if (cautionValue >= warningValue || warningValue >= dangerValue) {
            throw new IllegalArgumentException("임계값은 관심 < 경고 < 위험 순서여야 합니다");
        }
        thresholdMapper.updateById(id, cautionValue, warningValue, dangerValue);
        refreshCache();
    }

    /** 캐시 갱신 (임계값 수정 시 호출) */
    public void refreshCache() {
        loadCache();
        log.info("임계값 캐시가 갱신되었습니다");
    }
}
