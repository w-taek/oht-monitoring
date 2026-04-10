package com.example.ohtmon.controller;

import com.example.ohtmon.common.ApiResponse;
import com.example.ohtmon.dto.MaintOrderDto;
import com.example.ohtmon.service.MaintOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintOrderController {

    private final MaintOrderService maintOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<MaintOrderDto.PageResponse>> getOrders(
            @RequestParam(required = false) String eqId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(maintOrderService.getOrders(eqId, status, page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createOrder(
            @Valid @RequestBody MaintOrderDto.CreateRequest request) {
        maintOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "정비 오더가 생성되었습니다"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintOrderDto.DetailResponse>> getOrder(
            @PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(maintOrderService.getOrder(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable long id,
            @Valid @RequestBody MaintOrderDto.StatusUpdateRequest request) {
        maintOrderService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "정비 오더 상태가 변경되었습니다"));
    }
}
