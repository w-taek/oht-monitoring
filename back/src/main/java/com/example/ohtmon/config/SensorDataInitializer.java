package com.example.ohtmon.config;

import com.example.ohtmon.mapper.SensorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataInitializer implements ApplicationRunner {

    private final SensorMapper sensorMapper;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        // 서버 시작 시 리플레이 알림 초기화 (데모용 데이터이므로)
        cleanupReplayAlerts();

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

    private void cleanupReplayAlerts() {
        try {
            // maint_order의 FK 참조가 있는 알림은 제외하고 삭제
            int deleted = jdbcTemplate.update(
                    "DELETE FROM alert_event WHERE id NOT IN (SELECT alert_event_id FROM maint_order WHERE alert_event_id IS NOT NULL)");
            if (deleted > 0) {
                log.info("리플레이 알림 초기화: {}건 삭제 (정비 오더 연결 건은 유지)", deleted);
            }
        } catch (Exception e) {
            log.warn("알림 초기화 실패 (무시 가능): {}", e.getMessage());
        }
    }
}
