package com.erp.montfortuganda.common.importframework.controller;

import com.erp.montfortuganda.common.importframework.engine.ImportFacade;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.model.ErpImportJob;
import com.erp.montfortuganda.common.importframework.service.ImportJobService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportFacade importFacade;
    private final ImportJobService importJobService;

    @PostMapping("/{moduleName}")
    public ResponseEntity<String> startImport(
            @PathVariable String moduleName,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "INSERT") ImportMode mode,
            @RequestParam Integer branchId,
            @RequestParam Long userId) {

        try {
            // 1. Calculate actual File Hash for Idempotency
            String fileHash;
            try (InputStream is = file.getInputStream()) {
                fileHash = DigestUtils.sha256Hex(is);
            }

            // 2. Safe, Collision-Free File Name supporting both CSV and XLSX
            String originalName = file.getOriginalFilename() != null ?
                    file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_") : "import.csv";
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalName;

            // 3. Save file to the temp directory
            Path tempPath = Path.of(System.getProperty("java.io.tmpdir"), uniqueFileName);
            file.transferTo(tempPath.toFile());

            // 4. Submit Job using the EXACT filename
            String jobId = importFacade.submitImportJob(
                    moduleName,
                    String.valueOf(branchId),
                    String.valueOf(userId),
                    mode,
                    fileHash,
                    uniqueFileName);

            return ResponseEntity.accepted().body(jobId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/progress/{jobId}")
    public ResponseEntity<ErpImportJob> getProgress(@PathVariable String jobId) {
        return importJobService.getJobStatus(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history/{moduleName}")
    public ResponseEntity<List<ErpImportJob>> getHistory(@PathVariable String moduleName) {
        return ResponseEntity.ok(importJobService.getRecentJobs(moduleName));
    }

    @GetMapping("/errors/{jobId}")
    public ResponseEntity<byte[]> downloadErrorReport(@PathVariable String jobId) {
        byte[] excelData = importJobService.generateErrorReport(jobId);
        if (excelData == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Error_Report_" + jobId + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}