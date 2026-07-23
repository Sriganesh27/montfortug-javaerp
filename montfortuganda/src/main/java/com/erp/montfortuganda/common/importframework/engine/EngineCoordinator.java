package com.erp.montfortuganda.common.importframework.engine;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.context.ImportSession;
import com.erp.montfortuganda.common.importframework.excel.GenericExcelReader;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import com.erp.montfortuganda.common.importframework.metrics.ImportMetricsCollector;
import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import com.erp.montfortuganda.common.importframework.model.ErpImportErrorRepository;
import com.erp.montfortuganda.common.importframework.model.ErpImportJobRepository;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;
import com.erp.montfortuganda.common.importframework.plugin.ImportPlugin;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;
import com.erp.montfortuganda.common.importframework.registry.ImportTemplate;
import lombok.RequiredArgsConstructor;
import java.util.Objects;
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

    private static final int ERROR_CODE_MAX_LENGTH = 50;
    private static final int COLUMN_NAME_MAX_LENGTH = 100;
    private static final int DATABASE_TEXT_MAX_LENGTH = 1000;

    private final GenericExcelReader excelReader;
    private final ImportMetricsCollector metricsCollector;
    private final ErpImportErrorRepository errorRepository;
    private final ErpImportJobRepository jobRepository;

    @Transactional
    public <DTO> void executeJob(
            ImportContext context,
            ImportSession session,
            ImportPlugin<DTO> plugin,
            Path filePath,
            ImportTemplate template
    ) {
        validateInput(
                context,
                session,
                plugin,
                filePath,
                template
        );

        log.info(
                "EngineCoordinator starting job {} for module {}",
                context.getJobId(),
                plugin.getManifest().getModuleName()
        );

        session.setCurrentLifecycle(
                ImportStatus.INITIALIZING
        );

        executeBeforeImportHook(
                context,
                session,
                plugin
        );

        session.setCurrentLifecycle(
                ImportStatus.READING_ROWS
        );

        /*
         * Excel row 1 contains headers, so the first data row will
         * receive row number 2.
         */
        AtomicInteger globalRowCounter =
                new AtomicInteger(1);

        AtomicInteger totalSucceeded =
                new AtomicInteger(0);

        AtomicInteger totalValidationFailed =
                new AtomicInteger(0);

        AtomicInteger totalProcessingFailed =
                new AtomicInteger(0);

        long startedAt =
                System.currentTimeMillis();

        try {
            excelReader.processFileInChunks(
                    filePath,
                    context,
                    template,
                    rawChunk -> processChunk(
                            rawChunk,
                            context,
                            session,
                            plugin,
                            globalRowCounter,
                            totalSucceeded,
                            totalValidationFailed,
                            totalProcessingFailed
                    )
            );

            int totalFailed =
                    totalValidationFailed.get()
                            + totalProcessingFailed.get();

            session.setCurrentLifecycle(
                    totalFailed > 0
                            ? ImportStatus.COMPLETED_WITH_ERRORS
                            : ImportStatus.COMPLETED
            );

            session.setProcessedRows(
                    globalRowCounter.get() - 1
            );

            session.setSuccessRows(
                    totalSucceeded.get()
            );

            session.setFailedRows(
                    totalFailed
            );

            /*
             * Push the final result to the database before
             * recording final metrics.
             */
            updateProgress(
                    context,
                    session
            );

            ChunkProcessingResult finalResult =
                    ChunkProcessingResult.builder()
                            .processed(
                                    session.getProcessedRows()
                            )
                            .succeeded(
                                    session.getSuccessRows()
                            )
                            .validationFailed(
                                    totalValidationFailed.get()
                            )
                            .processingFailed(
                                    totalProcessingFailed.get()
                            )
                            .processingTimeMs(
                                    System.currentTimeMillis()
                                            - startedAt
                            )
                            .build();

            metricsCollector.recordFinalMetrics(
                    context,
                    finalResult
            );

            log.info(
                    "EngineCoordinator successfully completed job {}. "
                            + "Processed: {}, successful: {}, failed: {}",
                    context.getJobId(),
                    session.getProcessedRows(),
                    session.getSuccessRows(),
                    session.getFailedRows()
            );
        } catch (Exception exception) {
            log.error(
                    "EngineCoordinator encountered a fatal system "
                            + "error for job {}",
                    context.getJobId(),
                    exception
            );

            session.setCurrentLifecycle(
                    ImportStatus.FAILED
            );

            /*
             * Attempt to publish the failed status. A failure here
             * must not hide the original import exception.
             */
            try {
                updateProgress(
                        context,
                        session
                );
            } catch (Exception progressException) {
                log.error(
                        "Unable to publish failed progress for import job {}",
                        context.getJobId(),
                        progressException
                );
            }

            throw new RuntimeException(
                    "Import job execution failed.",
                    exception
            );
        }
    }

    private <DTO> void processChunk(
            List<Map<String, String>> rawChunk,
            ImportContext context,
            ImportSession session,
            ImportPlugin<DTO> plugin,
            AtomicInteger globalRowCounter,
            AtomicInteger totalSucceeded,
            AtomicInteger totalValidationFailed,
            AtomicInteger totalProcessingFailed
    ) {
        if (
                rawChunk == null
                        || rawChunk.isEmpty()
        ) {
            return;
        }

        session.setCurrentLifecycle(
                ImportStatus.VALIDATING_ROWS
        );

        List<DTO> validRows =
                new ArrayList<>();

        int chunkValidationErrors = 0;

        for (Map<String, String> rowData : rawChunk) {
            int currentRowNumber =
                    globalRowCounter.incrementAndGet();

            DTO dto;

            try {
                dto =
                        plugin.getRowMapper()
                                .mapRow(
                                        rowData,
                                        currentRowNumber
                                );
            } catch (RuntimeException exception) {
                chunkValidationErrors++;

                saveMappingError(
                        context,
                        currentRowNumber,
                        exception
                );

                log.warn(
                        "Import row mapping failed. Job: {}, row: {}, "
                                + "exception: {}",
                        context.getJobId(),
                        currentRowNumber,
                        exception.getClass().getSimpleName()
                );

                continue;
            }

            ValidationResult validationResult;

            try {
                validationResult =
                        plugin.getValidator()
                                .validate(
                                        dto,
                                        currentRowNumber,
                                        context
                                );
            } catch (RuntimeException exception) {
                chunkValidationErrors++;

                saveValidationException(
                        context,
                        currentRowNumber,
                        exception
                );

                log.warn(
                        "Import row validation threw an exception. "
                                + "Job: {}, row: {}, exception: {}",
                        context.getJobId(),
                        currentRowNumber,
                        exception.getClass().getSimpleName()
                );

                continue;
            }

            if (validationResult == null) {
                chunkValidationErrors++;

                saveFrameworkValidationError(
                        context,
                        currentRowNumber,
                        "VALIDATION_RESULT_MISSING",
                        "The row validator returned no validation result.",
                        "Review the import validator implementation."
                );

                continue;
            }

            if (validationResult.isSkipRow()) {
                log.debug(
                        "Skipping import row {} for job {}",
                        currentRowNumber,
                        context.getJobId()
                );

                continue;
            }

            if (validationResult.isSuccess()) {
                validRows.add(dto);
                continue;
            }

            chunkValidationErrors++;

            saveValidationErrors(
                    context,
                    currentRowNumber,
                    validationResult
            );
        }

        session.setCurrentLifecycle(
                ImportStatus.SAVING_BATCH
        );

        ChunkProcessingResult chunkResult =
                processValidRows(
                        validRows,
                        context,
                        plugin
                );

        saveProcessingErrors(
                chunkResult,
                context
        );

        totalSucceeded.addAndGet(
                safeCount(
                        chunkResult.getSucceeded()
                )
        );

        totalValidationFailed.addAndGet(
                chunkValidationErrors
                        + safeCount(
                        chunkResult.getValidationFailed()
                )
        );

        totalProcessingFailed.addAndGet(
                safeCount(
                        chunkResult.getProcessingFailed()
                )
        );

        metricsCollector.recordChunkMetrics(
                context,
                chunkResult
        );

        session.setProcessedRows(
                globalRowCounter.get() - 1
        );

        session.setSuccessRows(
                totalSucceeded.get()
        );

        session.setFailedRows(
                totalValidationFailed.get()
                        + totalProcessingFailed.get()
        );

        updateProgress(
                context,
                session
        );
    }

    private void saveValidationErrors(
            ImportContext context,
            int currentRowNumber,
            ValidationResult validationResult
    ) {
        if (
                validationResult.getErrors() == null
                        || validationResult.getErrors().isEmpty()
        ) {
            saveFrameworkValidationError(
                    context,
                    currentRowNumber,
                    "VALIDATION_FAILED",
                    "The row failed validation.",
                    "Correct the row values and retry the import."
            );

            return;
        }

        List<ErpImportError> errors =
                validationResult.getErrors()
                        .stream()
                        .map(validationError -> {
                            String normalizedCode =
                                    normalizeErrorCode(
                                            validationError
                                                    .getErrorCode()
                                    );

                            log.warn(
                                    "Import validation error: "
                                            + "job={}, row={}, code={}, "
                                            + "codeLength={}, column={}",
                                    context.getJobId(),
                                    currentRowNumber,
                                    normalizedCode,
                                    normalizedCode.length(),
                                    validationError
                                            .getColumnName()
                            );

                            return ErpImportError.builder()
                                    .jobId(
                                            context.getJobId()
                                    )
                                    .rowNumber(
                                            currentRowNumber
                                    )
                                    .columnName(
                                            truncate(
                                                    validationError
                                                            .getColumnName(),
                                                    COLUMN_NAME_MAX_LENGTH
                                            )
                                    )
                                    .cellValue(
                                            truncate(
                                                    validationError
                                                            .getCellValue(),
                                                    DATABASE_TEXT_MAX_LENGTH
                                            )
                                    )
                                    .errorCode(
                                            normalizedCode
                                    )
                                    .severity(
                                            "ERROR"
                                    )
                                    .message(
                                            truncateWithFallback(
                                                    validationError
                                                            .getMessage(),
                                                    DATABASE_TEXT_MAX_LENGTH,
                                                    "The row failed validation."
                                            )
                                    )
                                    .suggestedFix(
                                            truncate(
                                                    validationError
                                                            .getSuggestedFix(),
                                                    DATABASE_TEXT_MAX_LENGTH
                                            )
                                    )
                                    .build();
                        })
                        .collect(
                                Collectors.toList()
                        );

        errorRepository.saveAll(
                errors
        );
    }

    private <DTO> ChunkProcessingResult processValidRows(
            List<DTO> validRows,
            ImportContext context,
            ImportPlugin<DTO> plugin
    ) {
        if (
                validRows == null
                        || validRows.isEmpty()
        ) {
            return emptyChunkResult();
        }

        return invokeProcessor(
                validRows,
                context,
                plugin
        );
    }

    private <DTO> ChunkProcessingResult invokeProcessor(
            List<DTO> validRows,
            ImportContext context,
            ImportPlugin<DTO> plugin
    ) {
        try {
            ChunkProcessingResult result =
                    plugin.getProcessor()
                            .processChunk(
                                    validRows,
                                    context
                            );

            if (result == null) {
                log.error(
                        "Import processor returned null for job {}",
                        context.getJobId()
                );

                return failedChunkResult(
                        validRows.size()
                );
            }

            return result;
        } catch (Exception exception) {
            log.error(
                    "Fatal error processing import chunk for job {}",
                    context.getJobId(),
                    exception
            );

            return failedChunkResult(
                    validRows.size()
            );
        }
    }

    private void saveProcessingErrors(
            ChunkProcessingResult chunkResult,
            ImportContext context
    ) {
        if (
                chunkResult == null
                        || chunkResult.getProcessingErrors() == null
                        || chunkResult.getProcessingErrors().isEmpty()
        ) {
            return;
        }

        List<ErpImportError> normalizedErrors =
                chunkResult.getProcessingErrors()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(error ->
                                normalizeProcessingError(
                                        error,
                                        context
                                )
                        )
                        .collect(Collectors.toList());


        if (!normalizedErrors.isEmpty()) {
            errorRepository.saveAll(
                    normalizedErrors
            );
        }
    }

    private ErpImportError normalizeProcessingError(
            ErpImportError error,
            ImportContext context
    ) {
        return ErpImportError.builder()
                .jobId(
                        hasText(
                                error.getJobId()
                        )
                                ? error.getJobId()
                                : context.getJobId()
                )
                .rowNumber(
                        error.getRowNumber()
                )
                .columnName(
                        truncate(
                                error.getColumnName(),
                                COLUMN_NAME_MAX_LENGTH
                        )
                )
                .cellValue(
                        truncate(
                                error.getCellValue(),
                                DATABASE_TEXT_MAX_LENGTH
                        )
                )
                .errorCode(
                        normalizeErrorCode(
                                error.getErrorCode()
                        )
                )
                .severity(
                        truncateWithFallback(
                                error.getSeverity(),
                                20,
                                "ERROR"
                        )
                )
                .message(
                        truncateWithFallback(
                                error.getMessage(),
                                DATABASE_TEXT_MAX_LENGTH,
                                "Employee row processing failed."
                        )
                )
                .suggestedFix(
                        truncate(
                                error.getSuggestedFix(),
                                DATABASE_TEXT_MAX_LENGTH
                        )
                )
                .build();
    }

    private void saveMappingError(
            ImportContext context,
            int rowNumber,
            RuntimeException exception
    ) {
        saveFrameworkValidationError(
                context,
                rowNumber,
                "ROW_MAPPING_FAILED",
                safeExceptionMessage(
                        exception,
                        "The Excel row could not be mapped."
                ),
                "Check the Excel values and template column formats."
        );
    }

    private void saveValidationException(
            ImportContext context,
            int rowNumber,
            RuntimeException exception
    ) {
        saveFrameworkValidationError(
                context,
                rowNumber,
                "ROW_VALIDATION_FAILED",
                safeExceptionMessage(
                        exception,
                        "The row could not be validated."
                ),
                "Correct the row values and retry the import."
        );
    }

    private void saveFrameworkValidationError(
            ImportContext context,
            int rowNumber,
            String errorCode,
            String message,
            String suggestedFix
    ) {
        ErpImportError error =
                ErpImportError.builder()
                        .jobId(
                                context.getJobId()
                        )
                        .rowNumber(
                                rowNumber
                        )
                        .columnName(
                                "Employee"
                        )
                        .cellValue(
                                "Excel row " + rowNumber
                        )
                        .errorCode(
                                normalizeErrorCode(
                                        errorCode
                                )
                        )
                        .severity(
                                "ERROR"
                        )
                        .message(
                                truncateWithFallback(
                                        message,
                                        DATABASE_TEXT_MAX_LENGTH,
                                        "The import row failed."
                                )
                        )
                        .suggestedFix(
                                truncate(
                                        suggestedFix,
                                        DATABASE_TEXT_MAX_LENGTH
                                )
                        )
                        .build();

        errorRepository.save(
                error
        );
    }

    private <DTO> void executeBeforeImportHook(
            ImportContext context,
            ImportSession session,
            ImportPlugin<DTO> plugin
    ) {
        if (plugin.getBeforeImportHook() == null) {
            return;
        }

        try {
            plugin.getBeforeImportHook()
                    .onBeforeImport(
                            context
                    );
        } catch (Exception exception) {
            log.error(
                    "Job {} aborted during pre-flight validation: {}",
                    context.getJobId(),
                    exception.getMessage(),
                    exception
            );

            session.setCurrentLifecycle(
                    ImportStatus.FAILED
            );

            throw new RuntimeException(
                    "Pre-flight validation failed.",
                    exception
            );
        }
    }

    private void updateProgress(
            ImportContext context,
            ImportSession session
    ) {
        jobRepository.updateProgress(
                context.getJobId(),
                session.getCurrentLifecycle(),
                session.getProcessedRows(),
                session.getSuccessRows(),
                session.getFailedRows()
        );
    }

    private ChunkProcessingResult emptyChunkResult() {
        return ChunkProcessingResult.builder()
                .processed(0)
                .succeeded(0)
                .validationFailed(0)
                .processingFailed(0)
                .processingTimeMs(0)
                .processingErrors(
                        List.of()
                )
                .build();
    }

    private ChunkProcessingResult failedChunkResult(
            int failedRows
    ) {
        return ChunkProcessingResult.builder()
                .processed(
                        failedRows
                )
                .succeeded(0)
                .validationFailed(0)
                .processingFailed(
                        failedRows
                )
                .processingTimeMs(0)
                .processingErrors(
                        List.of()
                )
                .build();
    }

    private String normalizeErrorCode(
            String errorCode
    ) {
        if (!hasText(errorCode)) {
            return "VALIDATION_ERROR";
        }

        String normalized =
                errorCode.trim()
                        .toUpperCase()
                        .replaceAll(
                                "[^A-Z0-9_]+",
                                "_"
                        )
                        .replaceAll(
                                "_+",
                                "_"
                        )
                        .replaceAll(
                                "^_|_$",
                                ""
                        );

        if (normalized.isBlank()) {
            return "VALIDATION_ERROR";
        }

        return truncate(
                normalized,
                ERROR_CODE_MAX_LENGTH
        );
    }

    private String safeExceptionMessage(
            RuntimeException exception,
            String fallback
    ) {
        if (
                exception == null
                        || !hasText(
                        exception.getMessage()
                )
        ) {
            return fallback;
        }

        String message =
                exception.getMessage()
                        .trim();

        String lowerMessage =
                message.toLowerCase();

        if (
                lowerMessage.contains("select ")
                        || lowerMessage.contains("insert ")
                        || lowerMessage.contains("update ")
                        || lowerMessage.contains("delete ")
                        || lowerMessage.contains("jdbc:")
                        || lowerMessage.contains("hibernate")
                        || lowerMessage.contains("java.io")
                        || lowerMessage.contains("java.nio")
        ) {
            return fallback;
        }

        return truncate(
                message,
                DATABASE_TEXT_MAX_LENGTH
        );
    }

    private String truncateWithFallback(
            String value,
            int maximumLength,
            String fallback
    ) {
        if (!hasText(value)) {
            return fallback;
        }

        return truncate(
                value,
                maximumLength
        );
    }

    private String truncate(
            String value,
            int maximumLength
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        if (
                normalized.length()
                        <= maximumLength
        ) {
            return normalized;
        }

        return normalized.substring(
                0,
                maximumLength
        );
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }

    private int safeCount(
            int count
    ) {
        return Math.max(
                count,
                0
        );
    }

    private <DTO> void validateInput(
            ImportContext context,
            ImportSession session,
            ImportPlugin<DTO> plugin,
            Path filePath,
            ImportTemplate template
    ) {
        if (context == null) {
            throw new IllegalArgumentException(
                    "Import context is required."
            );
        }

        if (
                context.getJobId() == null
                        || context.getJobId().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Import job ID is required."
            );
        }

        if (session == null) {
            throw new IllegalArgumentException(
                    "Import session is required."
            );
        }

        if (plugin == null) {
            throw new IllegalArgumentException(
                    "Import plugin is required."
            );
        }

        if (plugin.getManifest() == null) {
            throw new IllegalArgumentException(
                    "Import plugin manifest is required."
            );
        }

        if (plugin.getRowMapper() == null) {
            throw new IllegalArgumentException(
                    "Import row mapper is required."
            );
        }

        if (plugin.getValidator() == null) {
            throw new IllegalArgumentException(
                    "Import validator is required."
            );
        }

        if (plugin.getProcessor() == null) {
            throw new IllegalArgumentException(
                    "Import processor is required."
            );
        }

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "Import file path is required."
            );
        }

        if (template == null) {
            throw new IllegalArgumentException(
                    "Import template is required."
            );
        }
    }
}