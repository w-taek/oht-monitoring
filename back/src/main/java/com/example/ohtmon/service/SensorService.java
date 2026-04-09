package com.example.ohtmon.service;

import com.example.ohtmon.dto.SensorDto;
import com.example.ohtmon.mapper.SensorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorMapper sensorMapper;

    public SensorDto.Response getLatest(String eqId) {
        SensorDto.Response response = sensorMapper.findLatest(eqId);
        if (response == null) {
            throw new IllegalArgumentException("센서 데이터를 찾을 수 없습니다: " + eqId);
        }
        return response;
    }

    public List<SensorDto.Response> getHistory(String eqId, String from, String to) {
        return sensorMapper.findHistory(eqId, from, to);
    }

    public SensorDto.StateSummary getSummary() {
        List<SensorDto.StateCount> counts = sensorMapper.findStateSummary();

        int normal = 0, caution = 0, warning = 0, danger = 0;
        for (SensorDto.StateCount sc : counts) {
            switch (sc.getState()) {
                case 0 -> normal = sc.getCount();
                case 1 -> caution = sc.getCount();
                case 2 -> warning = sc.getCount();
                case 3 -> danger = sc.getCount();
            }
        }

        return SensorDto.StateSummary.builder()
                .normal(normal)
                .caution(caution)
                .warning(warning)
                .danger(danger)
                .build();
    }
}
