package com.example.ohtmon.mapper;

import com.example.ohtmon.domain.SensorReading;
import com.example.ohtmon.dto.SensorDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SensorMapper {

    SensorDto.Response findLatest(@Param("eqId") String eqId);

    List<SensorDto.Response> findHistory(@Param("eqId") String eqId,
                                         @Param("from") String from,
                                         @Param("to") String to);

    List<SensorDto.StateCount> findStateSummary();

    SensorReading findNextReplayData(@Param("eqId") String eqId,
                                     @Param("lastId") long lastId);

    long countByEqId(@Param("eqId") String eqId);

    long countAll();
}
