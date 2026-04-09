package com.example.ohtmon.config;

import com.example.ohtmon.mapper.SensorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorDataInitializer implements ApplicationRunner {

    private final SensorMapper sensorMapper;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
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
}
