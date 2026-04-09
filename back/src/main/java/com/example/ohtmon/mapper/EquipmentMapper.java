package com.example.ohtmon.mapper;

import com.example.ohtmon.domain.Equipment;
import com.example.ohtmon.dto.EquipmentDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EquipmentMapper {

    List<EquipmentDto.ListResponse> findAll(@Param("eqType") String eqType,
                                            @Param("status") String status);

    Equipment findById(@Param("eqId") String eqId);

    void insert(Equipment equipment);

    void updateStatus(@Param("eqId") String eqId,
                      @Param("status") String status);

    List<EquipmentDto.StatusCount> countByStatus();
}
