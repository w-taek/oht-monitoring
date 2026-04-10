package com.example.ohtmon.service;

import com.example.ohtmon.dto.StatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final StatsService statsService;

    public byte[] generateReport() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);

            createSensorAvgSheet(workbook, headerStyle);
            createDangerRankingSheet(workbook, headerStyle);
            createHealthScoreSheet(workbook, headerStyle);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createSensorAvgSheet(XSSFWorkbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("센서 평균 요약");
        List<StatsDto.SensorAvg> data = statsService.getSensorAvg(null);

        String[] headers = {"장비ID", "PM10 평균", "PM10 최대", "PM2.5 평균", "NTC온도 평균", "NTC온도 최대",
                "CT1 평균", "CT2 평균", "CT3 평균", "CT4 평균", "IR최고온도 평균", "IR최고온도 최대"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (StatsDto.SensorAvg d : data) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(d.getEqId());
            setCellDouble(row, 1, d.getPm10Avg());
            setCellDouble(row, 2, d.getPm10Max());
            setCellDouble(row, 3, d.getPm25Avg());
            setCellDouble(row, 4, d.getNtcTempAvg());
            setCellDouble(row, 5, d.getNtcTempMax());
            setCellDouble(row, 6, d.getCt1Avg());
            setCellDouble(row, 7, d.getCt2Avg());
            setCellDouble(row, 8, d.getCt3Avg());
            setCellDouble(row, 9, d.getCt4Avg());
            setCellDouble(row, 10, d.getIrTempMaxAvg());
            setCellDouble(row, 11, d.getIrTempMaxMax());
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void createDangerRankingSheet(XSSFWorkbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("위험 랭킹");
        List<StatsDto.DangerRanking> data = statsService.getDangerRanking(40);

        String[] headers = {"순위", "장비ID", "장비명", "전체 측정수", "위험 횟수", "위험 비율(%)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (StatsDto.DangerRanking d : data) {
            Row row = sheet.createRow(rowIdx);
            row.createCell(0).setCellValue(rowIdx);
            row.createCell(1).setCellValue(d.getEqId());
            row.createCell(2).setCellValue(d.getEqName());
            row.createCell(3).setCellValue(d.getTotalCount());
            row.createCell(4).setCellValue(d.getDangerCount());
            row.createCell(5).setCellValue(d.getDangerRatio());
            rowIdx++;
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void createHealthScoreSheet(XSSFWorkbook workbook, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("건강 점수");
        List<StatsDto.HealthScore> data = statsService.getHealthScore(null);

        String[] headers = {"장비ID", "장비명", "건강 점수", "PM10 이탈(%)", "NTC 이탈(%)", "CT 이탈(%)", "IR 이탈(%)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (StatsDto.HealthScore d : data) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(d.getEqId());
            row.createCell(1).setCellValue(d.getEqName());
            row.createCell(2).setCellValue(d.getScore());
            row.createCell(3).setCellValue(d.getPm10Deviation());
            row.createCell(4).setCellValue(d.getNtcDeviation());
            row.createCell(5).setCellValue(d.getCtDeviation());
            row.createCell(6).setCellValue(d.getIrDeviation());
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private void setCellDouble(Row row, int col, Double value) {
        if (value != null) {
            row.createCell(col).setCellValue(value);
        } else {
            row.createCell(col).setCellValue(0);
        }
    }
}
