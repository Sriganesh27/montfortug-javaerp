package com.erp.montfortuganda.common.importframework.report;

import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import com.erp.montfortuganda.common.importframework.model.ErpImportErrorRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
@RequiredArgsConstructor
public class ErrorExcelReportGenerator {

    private final ErpImportErrorRepository errorRepository;

    public byte[] generateErrorReport(String jobId) {
        // SXSSFWorkbook limits RAM to 100 rows, writing the rest to temporary disk files
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Import Errors");
            createHeaderRow(sheet);

            int rowNum = 1;
            int page = 0;
            int size = 1000;
            Page<ErpImportError> errorPage;

            // Database Pagination loop to prevent fetching 50,000 entities into RAM
            do {
                errorPage = errorRepository.findByJobId(jobId, PageRequest.of(page, size));

                for (ErpImportError error : errorPage.getContent()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(error.getRowNumber());
                    row.createCell(1).setCellValue(error.getColumnName());
                    row.createCell(2).setCellValue(error.getCellValue());
                    row.createCell(3).setCellValue(error.getErrorCode());
                    row.createCell(4).setCellValue(error.getMessage());
                    row.createCell(5).setCellValue(error.getSuggestedFix());
                }
                page++;
            } while (errorPage.hasNext());

            if (rowNum == 1) { // No errors found
                return null;
            }

            workbook.write(out);
            workbook.dispose(); // Delete temp files backing SXSSFWorkbook

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate error report for Job ID " + jobId, e);
        }
    }

    private void createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Row Number");
        header.createCell(1).setCellValue("Column");
        header.createCell(2).setCellValue("Failed Value");
        header.createCell(3).setCellValue("Error Code");
        header.createCell(4).setCellValue("Error Message");
        header.createCell(5).setCellValue("Suggested Fix");
    }
}