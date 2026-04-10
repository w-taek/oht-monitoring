package com.example.ohtmon.controller;

import com.example.ohtmon.common.ApiResponse;
import com.example.ohtmon.domain.ThresholdRule;
import com.example.ohtmon.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/threshold")
@RequiredArgsConstructor
public class ThresholdController {

    private final ThresholdService thresholdService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ThresholdRule>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(thresholdService.getAllThresholds()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable int id,
            @RequestBody Map<String, Double> body) {
        double cautionValue = body.get("cautionValue");
        double warningValue = body.get("warningValue");
        double dangerValue = body.get("dangerValue");
        thresholdService.updateThreshold(id, cautionValue, warningValue, dangerValue);
        return ResponseEntity.ok(ApiResponse.success(null, "임계값이 수정되었습니다"));
    }
}
