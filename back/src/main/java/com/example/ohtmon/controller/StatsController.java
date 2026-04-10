package com.example.ohtmon.controller;

import com.example.ohtmon.common.ApiResponse;
import com.example.ohtmon.dto.StatsDto;
import com.example.ohtmon.service.ExcelExportService;
import com.example.ohtmon.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final ExcelExportService excelExportService;

    @GetMapping("/sensor-avg")
    public ResponseEntity<ApiResponse<List<StatsDto.SensorAvg>>> getSensorAvg(
            @RequestParam(required = false) String eqId) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getSensorAvg(eqId)));
    }

    @GetMapping("/state-distribution")
    public ResponseEntity<ApiResponse<List<StatsDto.StateDistribution>>> getStateDistribution() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getStateDistribution()));
    }

    @GetMapping("/danger-ranking")
    public ResponseEntity<ApiResponse<List<StatsDto.DangerRanking>>> getDangerRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getDangerRanking(limit)));
    }

    @GetMapping("/health-score")
    public ResponseEntity<ApiResponse<List<StatsDto.HealthScore>>> getHealthScore(
            @RequestParam(required = false) String eqId) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getHealthScore(eqId)));
    }

    @GetMapping("/sensor-trend")
    public ResponseEntity<ApiResponse<List<StatsDto.SensorTrend>>> getSensorTrend(
            @RequestParam String eqId,
            @RequestParam String sensor) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getSensorTrend(eqId, sensor)));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] data = excelExportService.generateReport();
        String filename = "OHT_Report_" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(data);
    }
}
