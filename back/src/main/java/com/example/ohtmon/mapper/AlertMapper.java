package com.example.ohtmon.mapper;

import com.example.ohtmon.domain.AlertEvent;
import com.example.ohtmon.dto.AlertDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlertMapper {

    void insert(AlertEvent alertEvent);

    List<AlertDto.ListResponse> findAll(@Param("eqId") String eqId,
                                        @Param("level") String level,
                                        @Param("offset") int offset,
                                        @Param("size") int size);

    long countAll(@Param("eqId") String eqId,
                  @Param("level") String level);

    void acknowledge(@Param("id") long id);
}
