package com.example.ohtmon.controller;

import com.example.ohtmon.common.ApiResponse;
import com.example.ohtmon.dto.SensorDto;
import com.example.ohtmon.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sensor")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @GetMapping("/{eqId}/latest")
    public ResponseEntity<ApiResponse<SensorDto.Response>> getLatest(
            @PathVariable String eqId) {
        return ResponseEntity.ok(ApiResponse.success(sensorService.getLatest(eqId)));
    }

    @GetMapping("/{eqId}/history")
    public ResponseEntity<ApiResponse<List<SensorDto.Response>>> getHistory(
            @PathVariable String eqId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(ApiResponse.success(sensorService.getHistory(eqId, from, to)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SensorDto.StateSummary>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(sensorService.getSummary()));
    }
}
