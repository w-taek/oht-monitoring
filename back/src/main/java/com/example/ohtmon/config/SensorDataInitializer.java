package com.example.ohtmon.config;

import com.example.ohtmon.mapper.SensorMapper;
import com.example.ohtmon.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataInitializer implements ApplicationRunner {

    private final SensorMapper sensorMapper;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final AlertService alertService;

    @Override
    public void run(ApplicationArguments args) {
        // 서버 시작 시 리플레이 알림 초기화 (데모용 데이터이므로)
        cleanupReplayAlerts();

        // 알림 억제 프라이밍 → 첫 리플레이 루프에서 DANGER 폭탄 방지
        primeAlertSuppression();

        long count = sensorMapper.countAll();
        if (count > 0) {
            log.info("센서 데이터가 이미 존재합니다 ({}건). 초기화를 건너뜁니다.", count);
            return;
        }

        log.info("센서 데이터가 비어있습니다. sensor-data.sql 로딩 시작...");
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("sensor-data.sql"));
            populator.setSeparator(";");
            populator.execute(dataSource);
            long loaded = sensorMapper.countAll();
            log.info("센서 더미 데이터 로딩 완료: {}건", loaded);
        } catch (Exception e) {
            log.error("센서 데이터 로딩 실패", e);
        }
    }

    private void primeAlertSuppression() {
        List<String> eqIds = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            eqIds.add(String.format("OHT-%02d", i));
            eqIds.add(String.format("AGV-%02d", i));
        }
        alertService.primeSuppressionAll(eqIds);
    }

    private void cleanupReplayAlerts() {
        try {
            // FK 순서: maint_order(alert_event_id FK) → alert_event
            int maintDeleted = jdbcTemplate.update("DELETE FROM maint_order");
            int alertDeleted = jdbcTemplate.update("DELETE FROM alert_event");
            log.info("데모 데이터 초기화: 정비 오더 {}건, 알림 {}건 삭제", maintDeleted, alertDeleted);
        } catch (Exception e) {
            log.warn("데모 데이터 초기화 실패 (무시 가능): {}", e.getMessage());
        }
    }
}
