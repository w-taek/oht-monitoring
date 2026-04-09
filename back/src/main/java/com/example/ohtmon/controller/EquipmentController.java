package com.example.ohtmon.controller;

import com.example.ohtmon.common.ApiResponse;
import com.example.ohtmon.dto.EquipmentDto;
import com.example.ohtmon.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EquipmentDto.ListResponse>>> getEquipmentList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        List<EquipmentDto.ListResponse> list = equipmentService.getEquipmentList(type, status);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{eqId}")
    public ResponseEntity<ApiResponse<EquipmentDto.DetailResponse>> getEquipment(
            @PathVariable String eqId) {
        EquipmentDto.DetailResponse detail = equipmentService.getEquipment(eqId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createEquipment(
            @Valid @RequestBody EquipmentDto.CreateRequest request) {
        equipmentService.createEquipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "장비가 등록되었습니다"));
    }

    @PatchMapping("/{eqId}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable String eqId,
            @Valid @RequestBody EquipmentDto.StatusUpdateRequest request) {
        equipmentService.updateStatus(eqId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(null, "장비 상태가 변경되었습니다"));
    }
}
