package com.erp.montfortuganda.common.importframework.engine;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.context.ImportSession;
import com.erp.montfortuganda.common.importframework.excel.GenericExcelReader;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import com.erp.montfortuganda.common.importframework.metrics.ImportMetricsCollector;
import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import com.erp.montfortuganda.common.importframework.model.ErpImportErrorRepository;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;
import com.erp.montfortuganda.common.importframework.plugin.ImportPlugin;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;
import com.erp.montfortuganda.common.importframework.registry.ImportTemplate;
import com.erp.montfortuganda.common.importframework.service.ImportProgressTransactionService;
import com.erp.montfortuganda.common.importframework.service.RetryWorkbookMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EngineCoordinator {

    private static final int ERROR_CODE_MAX_LENGTH = 50;
    private static final int COLUMN_NAME_MAX_LENGTH = 100;
    private static final int DATABASE_TEXT_MAX_LENGTH = 1000;

    /**
     * Avoids one database update for every row while still keeping the
     * frontend counters visibly live.
     */
    private static final int LIVE_PROGRESS_ROW_INTERVAL = 10;
    private static final long LIVE_PROGRESS_MIN_INTERVAL_MS = 5_000L;

    /**
     * Plugin processors return aggregate counters only after their complete
     * input list finishes. Small internal batches keep success and failure
     * counters visibly live without changing row-level transactions.
     */
    private static final int LIVE_PROCESSING_BATCH_SIZE = 5;

    private final GenericExcelReader excelReader;
    private final ImportMetricsCollector metricsCollector;
    private final ErpImportErrorRepository errorRepository;
    private final ImportProgressTransactionService progressTransactionService;

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

        AtomicInteger totalRowCounter =
                new AtomicInteger(0);

        AtomicInteger processedRowCounter =
                new AtomicInteger(0);

        AtomicInteger totalSucceeded =
                new AtomicInteger(0);

        AtomicInteger totalValidationFailed =
                new AtomicInteger(0);

        AtomicInteger totalProcessingFailed =
                new AtomicInteger(0);

        AtomicLong lastProgressPublishedAt =
                new AtomicLong(0L);

        long startedAt =
                System.currentTimeMillis();

        try {
            session.setCurrentLifecycle(
                    ImportStatus.INITIALIZING
            );

            updateProgress(
                    context,
                    session,
                    0
            );

            executeBeforeImportHook(
                    context,
                    session,
                    plugin
            );

            /*
             * A lightweight first SAX/CSV pass calculates the exact number
             * of selected non-header rows before any Student or Employee is
             * validated or saved. The reader remains streaming, so the
             * complete workbook is never loaded into memory.
             */
            session.setCurrentLifecycle(
                    ImportStatus.READING_ROWS
            );

            updateProgress(
                    context,
                    session,
                    0
            );

            int totalRows =
                    countSelectedRows(
                            filePath,
                            context,
                            template
                    );

            totalRowCounter.set(
                    totalRows
            );

            session.setCurrentLifecycle(
                    ImportStatus.VALIDATING_ROWS
            );

            updateProgress(
                    context,
                    session,
                    totalRows
            );

            lastProgressPublishedAt.set(
                    System.currentTimeMillis()
            );

            /*
             * The second streaming pass performs the real import. It uses
             * the exact same GenericExcelReader, template validation and
             * target-row restrictions as the count pass.
             */
            excelReader.processFileInChunks(
                    filePath,
                    context,
                    template,
                    rawChunk -> processChunk(
                            rawChunk,
                            context,
                            session,
                            plugin,
                            totalRowCounter,
                            processedRowCounter,
                            totalSucceeded,
                            totalValidationFailed,
                            totalProcessingFailed,
                            lastProgressPublishedAt
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

            updateSessionCounters(
                    session,
                    processedRowCounter,
                    totalSucceeded,
                    totalValidationFailed,
                    totalProcessingFailed
            );

            updateProgress(
                    context,
                    session,
                    totalRows
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
                            .processingErrors(
                                    List.of()
                            )
                            .build();

            metricsCollector.recordFinalMetrics(
                    context,
                    finalResult
            );

            log.info(
                    "EngineCoordinator successfully completed job {}. "
                            + "Total: {}, processed: {}, successful: {}, failed: {}",
                    context.getJobId(),
                    totalRows,
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

            updateSessionCounters(
                    session,
                    processedRowCounter,
                    totalSucceeded,
                    totalValidationFailed,
                    totalProcessingFailed
            );

            try {
                updateProgress(
                        context,
                        session,
                        totalRowCounter.get()
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

    /**
     * Counts rows using the same streaming reader and secure target-row
     * restrictions used by the real import.
     */
    private int countSelectedRows(
            Path filePath,
            ImportContext context,
            ImportTemplate template
    ) throws Exception {
        AtomicInteger rowCount =
                new AtomicInteger(0);

        excelReader.processFileInChunks(
                filePath,
                context,
                template,
                rawChunk -> {
                    if (rawChunk != null) {
                        rowCount.addAndGet(
                                rawChunk.size()
                        );
                    }
                }
        );

        return rowCount.get();
    }

    private <DTO> void processChunk(
            List<Map<String, String>> rawChunk,
            ImportContext context,
            ImportSession session,
            ImportPlugin<DTO> plugin,
            AtomicInteger totalRowCounter,
            AtomicInteger processedRowCounter,
            AtomicInteger totalSucceeded,
            AtomicInteger totalValidationFailed,
            AtomicInteger totalProcessingFailed,
            AtomicLong lastProgressPublishedAt
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

        for (Map<String, String> rowData : rawChunk) {
            int processedSequence =
                    processedRowCounter.incrementAndGet();

            int currentRowNumber;

            Map<String, String> businessRowData;

            try {
                int workbookRowNumber =
                        extractPhysicalRowNumber(
                                rowData
                        );

                currentRowNumber =
                        resolveReportedRowNumber(
                                context,
                                workbookRowNumber
                        );

                businessRowData =
                        removeFrameworkMetadata(
                                rowData
                        );
            } catch (RuntimeException exception) {
                totalValidationFailed.incrementAndGet();

                int fallbackRowNumber =
                        processedSequence + 1;

                saveFrameworkValidationError(
                        context,
                        fallbackRowNumber,
                        "ROW_NUMBER_METADATA_INVALID",
                        "The physical spreadsheet row number is missing or invalid.",
                        "Upload the file again using the latest import template."
                );

                log.warn(
                        "Import row metadata is invalid. Job: {}, "
                                + "sequence: {}, exception: {}",
                        context.getJobId(),
                        processedSequence,
                        exception.getClass()
                                .getSimpleName()
                );

                publishLiveProgressIfDue(
                        context,
                        session,
                        totalRowCounter,
                        processedRowCounter,
                        totalSucceeded,
                        totalValidationFailed,
                        totalProcessingFailed,
                        lastProgressPublishedAt,
                        false
                );

                continue;
            }

            DTO dto;

            try {
                dto =
                        plugin.getRowMapper()
                                .mapRow(
                                        businessRowData,
                                        currentRowNumber
                                );
            } catch (RuntimeException exception) {
                totalValidationFailed.incrementAndGet();

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
                        exception.getClass()
                                .getSimpleName()
                );

                publishLiveProgressIfDue(
                        context,
                        session,
                        totalRowCounter,
                        processedRowCounter,
                        totalSucceeded,
                        totalValidationFailed,
                        totalProcessingFailed,
                        lastProgressPublishedAt,
                        false
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
                totalValidationFailed.incrementAndGet();

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
                        exception.getClass()
                                .getSimpleName()
                );

                publishLiveProgressIfDue(
                        context,
                        session,
                        totalRowCounter,
                        processedRowCounter,
                        totalSucceeded,
                        totalValidationFailed,
                        totalProcessingFailed,
                        lastProgressPublishedAt,
                        false
                );

                continue;
            }

            if (validationResult == null) {
                totalValidationFailed.incrementAndGet();

                saveFrameworkValidationError(
                        context,
                        currentRowNumber,
                        "VALIDATION_RESULT_MISSING",
                        "The row validator returned no validation result.",
                        "Review the import validator implementation."
                );

                publishLiveProgressIfDue(
                        context,
                        session,
                        totalRowCounter,
                        processedRowCounter,
                        totalSucceeded,
                        totalValidationFailed,
                        totalProcessingFailed,
                        lastProgressPublishedAt,
                        false
                );

                continue;
            }

            if (validationResult.isSkipRow()) {
                log.debug(
                        "Skipping import row {} for job {}",
                        currentRowNumber,
                        context.getJobId()
                );

                publishLiveProgressIfDue(
                        context,
                        session,
                        totalRowCounter,
                        processedRowCounter,
                        totalSucceeded,
                        totalValidationFailed,
                        totalProcessingFailed,
                        lastProgressPublishedAt,
                        false
                );

                continue;
            }

            if (validationResult.isSuccess()) {
                validRows.add(
                        dto
                );
            } else {
                totalValidationFailed.incrementAndGet();

                saveValidationErrors(
                        context,
                        currentRowNumber,
                        validationResult
                );
            }

            publishLiveProgressIfDue(
                    context,
                    session,
                    totalRowCounter,
                    processedRowCounter,
                    totalSucceeded,
                    totalValidationFailed,
                    totalProcessingFailed,
                    lastProgressPublishedAt,
                    false
            );
        }

        /*
         * Publish the validation counters before database processing starts.
         * This lets the frontend change from Validate to Process even when a
         * large processor transaction takes several seconds.
         */
        session.setCurrentLifecycle(
                ImportStatus.SAVING_BATCH
        );

        publishLiveProgressIfDue(
                context,
                session,
                totalRowCounter,
                processedRowCounter,
                totalSucceeded,
                totalValidationFailed,
                totalProcessingFailed,
                lastProgressPublishedAt,
                true
        );

        processValidRowsWithLiveProgress(
                validRows,
                context,
                session,
                plugin,
                totalRowCounter,
                processedRowCounter,
                totalSucceeded,
                totalValidationFailed,
                totalProcessingFailed,
                lastProgressPublishedAt
        );
    }

    /**
     * Processes valid DTOs in small internal batches so the frontend receives
     * successful and failed counters while database registration is still
     * running.
     *
     * <p>This does not change the plugin API, row-level REQUIRES_NEW
     * transactions, duplicate rules, reference caches or import hooks.</p>
     */
    private <DTO> void processValidRowsWithLiveProgress(
            List<DTO> validRows,
            ImportContext context,
            ImportSession session,
            ImportPlugin<DTO> plugin,
            AtomicInteger totalRowCounter,
            AtomicInteger processedRowCounter,
            AtomicInteger totalSucceeded,
            AtomicInteger totalValidationFailed,
            AtomicInteger totalProcessingFailed,
            AtomicLong lastProgressPublishedAt
    ) {
        if (
                validRows == null
                        || validRows.isEmpty()
        ) {
            publishLiveProgressIfDue(
                    context,
                    session,
                    totalRowCounter,
                    processedRowCounter,
                    totalSucceeded,
                    totalValidationFailed,
                    totalProcessingFailed,
                    lastProgressPublishedAt,
                    true
            );

            return;
        }

        for (
                int startIndex = 0;
                startIndex < validRows.size();
                startIndex += LIVE_PROCESSING_BATCH_SIZE
        ) {
            int endIndex =
                    Math.min(
                            startIndex
                                    + LIVE_PROCESSING_BATCH_SIZE,
                            validRows.size()
                    );

            List<DTO> processingBatch =
                    List.copyOf(
                            validRows.subList(
                                    startIndex,
                                    endIndex
                            )
                    );

            ChunkProcessingResult batchResult =
                    processValidRows(
                            processingBatch,
                            context,
                            plugin
                    );

            saveProcessingErrors(
                    batchResult,
                    context
            );

            totalSucceeded.addAndGet(
                    safeCount(
                            batchResult.getSucceeded()
                    )
            );

            totalValidationFailed.addAndGet(
                    safeCount(
                            batchResult.getValidationFailed()
                    )
            );

            totalProcessingFailed.addAndGet(
                    safeCount(
                            batchResult.getProcessingFailed()
                    )
            );

            metricsCollector.recordChunkMetrics(
                    context,
                    batchResult
            );

            publishLiveProgressIfDue(
                    context,
                    session,
                    totalRowCounter,
                    processedRowCounter,
                    totalSucceeded,
                    totalValidationFailed,
                    totalProcessingFailed,
                    lastProgressPublishedAt,
                    true
            );
        }
    }

    /**
     * Publishes row counters at a controlled rate. Forced calls are used
     * when a lifecycle phase changes or a chunk completes.
     */
    private void publishLiveProgressIfDue(
            ImportContext context,
            ImportSession session,
            AtomicInteger totalRowCounter,
            AtomicInteger processedRowCounter,
            AtomicInteger totalSucceeded,
            AtomicInteger totalValidationFailed,
            AtomicInteger totalProcessingFailed,
            AtomicLong lastProgressPublishedAt,
            boolean force
    ) {
        int processedRows =
                processedRowCounter.get();

        long now =
                System.currentTimeMillis();

        boolean rowIntervalReached =
                processedRows > 0
                        && processedRows
                        % LIVE_PROGRESS_ROW_INTERVAL
                        == 0;

        boolean timeIntervalReached =
                now - lastProgressPublishedAt.get()
                        >= LIVE_PROGRESS_MIN_INTERVAL_MS;

        if (
                !force
                        && !rowIntervalReached
                        && !timeIntervalReached
        ) {
            return;
        }

        updateSessionCounters(
                session,
                processedRowCounter,
                totalSucceeded,
                totalValidationFailed,
                totalProcessingFailed
        );

        updateProgress(
                context,
                session,
                totalRowCounter.get()
        );

        lastProgressPublishedAt.set(
                now
        );
    }

    private void updateSessionCounters(
            ImportSession session,
            AtomicInteger processedRowCounter,
            AtomicInteger totalSucceeded,
            AtomicInteger totalValidationFailed,
            AtomicInteger totalProcessingFailed
    ) {
        session.setProcessedRows(
                processedRowCounter.get()
        );

        session.setSuccessRows(
                totalSucceeded.get()
        );

        session.setFailedRows(
                totalValidationFailed.get()
                        + totalProcessingFailed.get()
        );
    }

    // =====================================================================
    // ROW NUMBER METADATA
    // =====================================================================

    private int extractPhysicalRowNumber(
            Map<String, String> rowData
    ) {
        if (rowData == null) {
            throw new IllegalArgumentException(
                    "Import row data is missing."
            );
        }

        String rowNumberValue =
                rowData.get(
                        GenericExcelReader
                                .ROW_NUMBER_METADATA_KEY
                );

        if (
                rowNumberValue == null
                        || rowNumberValue.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Spreadsheet row number metadata is missing."
            );
        }

        try {
            int rowNumber =
                    Integer.parseInt(
                            rowNumberValue.trim()
                    );

            if (rowNumber <= 0) {
                throw new NumberFormatException(
                        "Row number must be positive."
                );
            }

            return rowNumber;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Spreadsheet row number metadata is invalid.",
                    exception
            );
        }
    }

    /**
     * Converts a compact failed-only workbook row back to its physical row in
     * the original import workbook.
     *
     * <p>Normal imports do not contain retry metadata and therefore retain
     * their current physical row number unchanged.</p>
     */
    private int resolveReportedRowNumber(
            ImportContext context,
            int workbookRowNumber
    ) {
        if (
                context.getImportMode()
                        != ImportMode.RETRY_FAILED_ROWS
        ) {
            return workbookRowNumber;
        }

        Map<Integer, Integer> originalRowMapping =
                requireRetryRowMapping(
                        context
                );

        Integer originalRowNumber =
                originalRowMapping.get(
                        workbookRowNumber
                );

        if (originalRowNumber == null) {
            throw new IllegalArgumentException(
                    "Secure retry metadata does not contain workbook row "
                            + workbookRowNumber
                            + "."
            );
        }

        if (originalRowNumber <= 1) {
            throw new IllegalArgumentException(
                    "Secure retry metadata contains an invalid original "
                            + "Excel row."
            );
        }

        return originalRowNumber;
    }

    /**
     * Reads the backend-verified compact-to-original row mapping stored in
     * ImportContext.
     */
    private Map<Integer, Integer> requireRetryRowMapping(
            ImportContext context
    ) {
        Map<String, Object> options =
                context.getImportOptions();

        if (options == null) {
            throw new IllegalArgumentException(
                    "Secure retry options are missing."
            );
        }

        Object rawMapping =
                options.get(
                        RetryWorkbookMetadataService
                                .RETRY_ROW_MAPPING_OPTION
                );

        if (!(rawMapping instanceof Map<?, ?> mapping)) {
            throw new IllegalArgumentException(
                    "Secure retry row mapping is missing."
            );
        }

        Map<Integer, Integer> normalizedMapping =
                new HashMap<>();

        for (
                Map.Entry<?, ?> entry
                : mapping.entrySet()
        ) {
            int workbookRowNumber =
                    positiveRowNumber(
                            entry.getKey(),
                            "retry workbook row"
                    );

            int originalRowNumber =
                    positiveRowNumber(
                            entry.getValue(),
                            "original Excel row"
                    );

            Integer previous =
                    normalizedMapping.putIfAbsent(
                            workbookRowNumber,
                            originalRowNumber
                    );

            if (
                    previous != null
                            && previous != originalRowNumber
            ) {
                throw new IllegalArgumentException(
                        "Secure retry row mapping contains duplicate "
                                + "workbook rows."
                );
            }
        }

        if (normalizedMapping.isEmpty()) {
            throw new IllegalArgumentException(
                    "Secure retry row mapping is empty."
            );
        }

        return Map.copyOf(
                normalizedMapping
        );
    }

    private int positiveRowNumber(
            Object value,
            String label
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Secure "
                            + label
                            + " is missing."
            );
        }

        try {
            int rowNumber;

            if (value instanceof Number number) {
                rowNumber =
                        number.intValue();
            } else {
                rowNumber =
                        Integer.parseInt(
                                value.toString()
                                        .trim()
                        );
            }

            if (rowNumber <= 1) {
                throw new NumberFormatException(
                        "Data row must be greater than one."
                );
            }

            return rowNumber;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "Secure "
                            + label
                            + " is invalid.",
                    exception
            );
        }
    }

    private Map<String, String> removeFrameworkMetadata(
            Map<String, String> rowData
    ) {
        Map<String, String> businessRowData =
                new HashMap<>(
                        rowData
                );

        businessRowData.remove(
                GenericExcelReader
                        .ROW_NUMBER_METADATA_KEY
        );

        return businessRowData;
    }

    // =====================================================================
    // VALIDATION ERRORS
    // =====================================================================

    private void saveValidationErrors(
            ImportContext context,
            int currentRowNumber,
            ValidationResult validationResult
    ) {
        if (
                validationResult.getErrors() == null
                        || validationResult.getErrors()
                        .isEmpty()
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

    // =====================================================================
    // PROCESSING
    // =====================================================================

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
                        || chunkResult.getProcessingErrors()
                        .isEmpty()
        ) {
            return;
        }

        List<ErpImportError> normalizedErrors =
                chunkResult.getProcessingErrors()
                        .stream()
                        .filter(
                                Objects::nonNull
                        )
                        .map(error ->
                                normalizeProcessingError(
                                        error,
                                        context
                                )
                        )
                        .collect(
                                Collectors.toList()
                        );

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
                                "Import row processing failed."
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

    // =====================================================================
    // FRAMEWORK ERRORS
    // =====================================================================

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
                        "The spreadsheet row could not be mapped."
                ),
                "Check the spreadsheet values and template column formats."
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
                                "Row"
                        )
                        .cellValue(
                                "Spreadsheet row "
                                        + rowNumber
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

    // =====================================================================
    // IMPORT LIFECYCLE
    // =====================================================================

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
            ImportSession session,
            int totalRows
    ) {
        progressTransactionService.updateProgress(
                context.getJobId(),
                session.getCurrentLifecycle(),
                Math.max(totalRows, 0),
                session.getProcessedRows(),
                session.getSuccessRows(),
                session.getFailedRows()
        );
    }

    // =====================================================================
    // CHUNK RESULTS
    // =====================================================================

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

    // =====================================================================
    // ERROR SAFETY
    // =====================================================================

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

    // =====================================================================
    // INPUT VALIDATION
    // =====================================================================

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
                        || context.getJobId()
                        .isBlank()
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


        if (
                context.getImportMode()
                        == ImportMode.RETRY_FAILED_ROWS
        ) {
            requireRetryRowMapping(
                    context
            );
        }
    }
}