package com.example.ohtmon.mapper;

import com.example.ohtmon.dto.StatsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatsMapper {

    List<StatsDto.SensorAvg> sensorAvgByEquipment(@Param("eqId") String eqId);

    List<StatsDto.StateDistribution> stateDistribution();

    List<StatsDto.DangerRanking> dangerRanking(@Param("limit") int limit);

    List<StatsDto.HealthScore> healthScoreRaw(@Param("eqId") String eqId,
                                              @Param("pm10Caution") double pm10Caution,
                                              @Param("ntcCaution") double ntcCaution,
                                              @Param("ctCaution") double ctCaution,
                                              @Param("irCaution") double irCaution);

    List<StatsDto.SensorTrend> sensorTrend(@Param("eqId") String eqId,
                                            @Param("sensorName") String sensorName);
}
