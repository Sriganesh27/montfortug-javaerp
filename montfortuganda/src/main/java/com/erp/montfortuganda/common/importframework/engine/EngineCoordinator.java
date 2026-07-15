package com.erp.montfortuganda.common.importframework.engine;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.context.ImportSession;
import com.erp.montfortuganda.common.importframework.excel.GenericExcelReader;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import com.erp.montfortuganda.common.importframework.metrics.ImportMetricsCollector;
import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;
import com.erp.montfortuganda.common.importframework.plugin.ImportPlugin;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;
import com.erp.montfortuganda.common.importframework.registry.ImportTemplate;
import com.erp.montfortuganda.common.importframework.model.ErpImportErrorRepository;
import com.erp.montfortuganda.common.importframework.model.ErpImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EngineCoordinator {

    private final GenericExcelReader excelReader;
    private final ImportMetricsCollector metricsCollector;
    private final ErpImportErrorRepository errorRepository;
    private final ErpImportJobRepository jobRepository; // Added for live progress updates

    @Transactional // Ensures atomic updates for progress pushing
    public <DTO> void executeJob(ImportContext context, ImportSession session, ImportPlugin<DTO> plugin, Path filePath, ImportTemplate template) {
        log.info("EngineCoordinator starting job {} for module {}", context.getJobId(), plugin.getManifest().getModuleName());

        session.setCurrentLifecycle(ImportStatus.INITIALIZING);

        if (plugin.getBeforeImportHook() != null) {
            try {
                plugin.getBeforeImportHook().onBeforeImport(context);
            } catch (Exception e) {
                log.error("Job {} aborted during pre-flight validation: {}", context.getJobId(), e.getMessage());
                session.setCurrentLifecycle(ImportStatus.FAILED);
                throw new RuntimeException("Pre-flight validation failed: " + e.getMessage(), e);
            }
        }

        session.setCurrentLifecycle(ImportStatus.READING_ROWS);

        AtomicInteger globalRowCounter = new AtomicInteger(1);
        AtomicInteger totalSucceeded = new AtomicInteger(0);
        AtomicInteger totalValidationFailed = new AtomicInteger(0);
        AtomicInteger totalProcessingFailed = new AtomicInteger(0);

        try {
            // Passing the File Path directly to support both CSV and XLSX with low memory
            excelReader.processFileInChunks(filePath, context, template, rawChunk -> {

                session.setCurrentLifecycle(ImportStatus.VALIDATING_ROWS);
                List<DTO> validDtos = new ArrayList<>();
                int chunkValidationErrors = 0;

                for (Map<String, String> rowData : rawChunk) {
                    int currentRowNum = globalRowCounter.incrementAndGet();
                    DTO dto = plugin.getRowMapper().mapRow(rowData, currentRowNum);

                    ValidationResult result = plugin.getValidator().validate(dto, currentRowNum, context);

                    if (result.isSuccess() && !result.isSkipRow()) {
                        validDtos.add(dto);
                    } else {
                        chunkValidationErrors += 1;
                        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                            List<ErpImportError> errors = result.getErrors().stream()
                                    .map(err -> ErpImportError.builder()
                                            .jobId(context.getJobId())
                                            .rowNumber(currentRowNum)
                                            .columnName(err.getColumnName())
                                            .cellValue(err.getCellValue())
                                            .errorCode(err.getErrorCode() != null ? err.getErrorCode() : "VALIDATION_ERROR")
                                            .severity("ERROR")
                                            .message(err.getMessage())
                                            .suggestedFix(err.getSuggestedFix())
                                            .build())
                                    .collect(Collectors.toList());
                            errorRepository.saveAll(errors);
                        }
                    }
                }

                session.setCurrentLifecycle(ImportStatus.SAVING_BATCH);

                ChunkProcessingResult chunkResult;
                try {
                    chunkResult = plugin.getProcessor().processChunk(validDtos, context);
                } catch (Exception e) {
                    log.error("FATAL ERROR processing chunk in job {}", context.getJobId(), e);
                    chunkResult = ChunkProcessingResult.builder()
                            .processed(validDtos.size())
                            .succeeded(0)
                            .validationFailed(0)
                            .processingFailed(validDtos.size())
                            .processingTimeMs(0)
                            .build();
                }

                // FIX: Save processing-stage database constraint errors to Excel Report
                if (chunkResult.getProcessingErrors() != null && !chunkResult.getProcessingErrors().isEmpty()) {
                    errorRepository.saveAll(chunkResult.getProcessingErrors());
                }

                totalSucceeded.addAndGet(chunkResult.getSucceeded());
                totalValidationFailed.addAndGet(chunkValidationErrors + chunkResult.getValidationFailed());
                totalProcessingFailed.addAndGet(chunkResult.getProcessingFailed());

                metricsCollector.recordChunkMetrics(context, chunkResult);

                session.setProcessedRows(globalRowCounter.get() - 1);
                session.setSuccessRows(totalSucceeded.get());
                session.setFailedRows(totalValidationFailed.get() + totalProcessingFailed.get());

                // FIX: Live atomic database update so frontend polling API actually updates the progress bar
                jobRepository.updateProgress(
                        context.getJobId(),
                        session.getCurrentLifecycle(),
                        session.getProcessedRows(),
                        session.getSuccessRows(),
                        session.getFailedRows()
                );
            });

            session.setCurrentLifecycle((totalValidationFailed.get() + totalProcessingFailed.get()) > 0 ?
                    ImportStatus.COMPLETED_WITH_ERRORS : ImportStatus.COMPLETED);

            ChunkProcessingResult finalResult = ChunkProcessingResult.builder()
                    .processed(session.getProcessedRows())
                    .succeeded(session.getSuccessRows())
                    .validationFailed(totalValidationFailed.get())
                    .processingFailed(totalProcessingFailed.get())
                    .processingTimeMs(System.currentTimeMillis() - session.getStartTime())
                    .build();

            metricsCollector.recordFinalMetrics(context, finalResult);
            log.info("EngineCoordinator successfully completed job {}", context.getJobId());

        } catch (Exception e) {
            log.error("EngineCoordinator encountered a fatal system error for job {}", context.getJobId(), e);
            session.setCurrentLifecycle(ImportStatus.FAILED);
            throw new RuntimeException(e);
        }
    }
}