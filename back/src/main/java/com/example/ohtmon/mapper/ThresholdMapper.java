package com.example.ohtmon.mapper;

import com.example.ohtmon.domain.ThresholdRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ThresholdMapper {

    List<ThresholdRule> findByEqType(@Param("eqType") String eqType);

    List<ThresholdRule> findAll();

    void updateById(@Param("id") int id,
                    @Param("cautionValue") double cautionValue,
                    @Param("warningValue") double warningValue,
                    @Param("dangerValue") double dangerValue);
}
