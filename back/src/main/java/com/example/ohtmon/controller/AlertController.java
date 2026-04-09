package com.example.ohtmon.controller;

import com.example.ohtmon.common.ApiResponse;
import com.example.ohtmon.dto.AlertDto;
import com.example.ohtmon.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<AlertDto.PageResponse>> getAlerts(
            @RequestParam(required = false) String eqId,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(alertService.getAlerts(eqId, level, page, size)));
    }

    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<Void>> acknowledge(@PathVariable long id) {
        alertService.acknowledge(id);
        return ResponseEntity.ok(ApiResponse.success(null, "알림이 확인 처리되었습니다"));
    }
}
