package com.example.ohtmon.service;

import com.example.ohtmon.domain.Equipment;
import com.example.ohtmon.dto.EquipmentDto;
import com.example.ohtmon.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentMapper equipmentMapper;

    public List<EquipmentDto.ListResponse> getEquipmentList(String eqType, String status) {
        return equipmentMapper.findAll(eqType, status);
    }

    public EquipmentDto.DetailResponse getEquipment(String eqId) {
        Equipment equipment = equipmentMapper.findById(eqId);
        if (equipment == null) {
            throw new IllegalArgumentException("장비를 찾을 수 없습니다: " + eqId);
        }
        return EquipmentDto.DetailResponse.builder()
                .eqId(equipment.getEqId())
                .eqName(equipment.getEqName())
                .eqType(equipment.getEqType())
                .manufacturer(equipment.getManufacturer())
                .location(equipment.getLocation())
                .status(equipment.getStatus())
                .installDate(equipment.getInstallDate())
                .createdAt(equipment.getCreatedAt())
                .build();
    }

    public void createEquipment(EquipmentDto.CreateRequest request) {
        if (equipmentMapper.findById(request.getEqId()) != null) {
            throw new IllegalArgumentException("이미 존재하는 장비 ID입니다: " + request.getEqId());
        }
        Equipment equipment = Equipment.builder()
                .eqId(request.getEqId())
                .eqName(request.getEqName())
                .eqType(request.getEqType())
                .manufacturer(request.getManufacturer())
                .location(request.getLocation())
                .installDate(request.getInstallDate())
                .build();
        equipmentMapper.insert(equipment);
    }

    public void updateStatus(String eqId, String status) {
        Equipment equipment = equipmentMapper.findById(eqId);
        if (equipment == null) {
            throw new IllegalArgumentException("장비를 찾을 수 없습니다: " + eqId);
        }
        equipmentMapper.updateStatus(eqId, status);
    }

    public List<EquipmentDto.StatusCount> getStatusCounts() {
        return equipmentMapper.countByStatus();
    }
}
