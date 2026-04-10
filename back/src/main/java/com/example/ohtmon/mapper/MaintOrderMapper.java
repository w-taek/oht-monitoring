package com.example.ohtmon.mapper;

import com.example.ohtmon.domain.MaintOrder;
import com.example.ohtmon.dto.MaintOrderDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MaintOrderMapper {

    List<MaintOrderDto.ListResponse> findAll(@Param("eqId") String eqId,
                                              @Param("status") String status,
                                              @Param("offset") int offset,
                                              @Param("size") int size);

    long countAll(@Param("eqId") String eqId,
                  @Param("status") String status);

    MaintOrderDto.DetailResponse findById(@Param("id") long id);

    void insert(MaintOrder maintOrder);

    void updateStatus(@Param("id") long id,
                      @Param("status") String status,
                      @Param("assignee") String assignee,
                      @Param("actionTaken") String actionTaken,
                      @Param("startedAt") LocalDateTime startedAt,
                      @Param("completedAt") LocalDateTime completedAt);
}
