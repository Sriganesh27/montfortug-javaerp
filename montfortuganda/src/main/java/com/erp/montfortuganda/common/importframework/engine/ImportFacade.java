package com.erp.montfortuganda.common.importframework.engine;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.context.ImportSession;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import com.erp.montfortuganda.common.importframework.model.ErpImportJob;
import com.erp.montfortuganda.common.importframework.model.ErpImportJobRepository;
import com.erp.montfortuganda.common.importframework.plugin.ImportPlugin;
import com.erp.montfortuganda.common.importframework.registry.ImportTemplate;
import com.erp.montfortuganda.common.importframework.registry.PluginRegistry;
import com.erp.montfortuganda.common.importframework.report.CorrectedWorkbookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class ImportFacade {

    private final PluginRegistry pluginRegistry;
    private final ErpImportJobRepository jobRepository;
    private final EngineCoordinator engineCoordinator;
    private final CorrectedWorkbookService correctedWorkbookService;
    private final ExecutorService executorService;

    public ImportFacade(
            PluginRegistry pluginRegistry,
            ErpImportJobRepository jobRepository,
            EngineCoordinator engineCoordinator,
            CorrectedWorkbookService correctedWorkbookService,
            @Qualifier("importVirtualThreadExecutor")
            ExecutorService executorService
    ) {
        this.pluginRegistry = pluginRegistry;
        this.jobRepository = jobRepository;
        this.engineCoordinator = engineCoordinator;
        this.correctedWorkbookService = correctedWorkbookService;
        this.executorService = executorService;
    }

    /**
     * Submits a normal import job.
     *
     * Retry mode cannot be submitted through this method because retry row
     * numbers must be obtained securely by the backend from the previous job.
     */
    public String submitImportJob(
            String module,
            String branchId,
            String userId,
            ImportMode mode,
            String fileHash,
            String uploadedFileName,
            Map<String, Object> importOptions
    ) {
        if (mode == ImportMode.RETRY_FAILED_ROWS) {
            throw new IllegalArgumentException(
                    "Retry Failed Rows must be submitted through "
                            + "the retry import endpoint."
            );
        }

        return submitImportJobInternal(
                module,
                branchId,
                userId,
                mode,
                fileHash,
                uploadedFileName,
                importOptions,
                null
        );
    }

    /**
     * Submits a retry job using failed physical Excel row numbers resolved by
     * the backend. Row numbers must never be accepted directly from a browser.
     */
    public String submitRetryJob(
            String module,
            String branchId,
            String userId,
            String fileHash,
            String uploadedFileName,
            Map<String, Object> importOptions,
            Set<Integer> targetRowNumbers
    ) {
        return submitImportJobInternal(
                module,
                branchId,
                userId,
                ImportMode.RETRY_FAILED_ROWS,
                fileHash,
                uploadedFileName,
                importOptions,
                targetRowNumbers
        );
    }

    private String submitImportJobInternal(
            String module,
            String branchId,
            String userId,
            ImportMode mode,
            String fileHash,
            String uploadedFileName,
            Map<String, Object> importOptions,
            Set<Integer> targetRowNumbers
    ) {
        validateSubmission(
                module,
                branchId,
                userId,
                mode,
                fileHash,
                uploadedFileName
        );

        Set<Integer> safeTargetRows =
                normalizeTargetRows(
                        mode,
                        targetRowNumbers
                );

        String normalizedModule =
                module.trim()
                        .toUpperCase();

        ImportPlugin<?> plugin =
                pluginRegistry.getPlugin(
                        normalizedModule
                );

        if (!plugin.getManifest()
                .getSupportedImportModes()
                .contains(mode)) {
            throw new IllegalArgumentException(
                    "Import mode "
                            + mode
                            + " is not supported for module "
                            + normalizedModule
                            + "."
            );
        }

        Map<String, Object> safeImportOptions =
                importOptions == null
                        ? Map.of()
                        : Map.copyOf(importOptions);

        jobRepository
                .findFirstByFileHashAndModuleAndBranchIdAndImportMode(
                        fileHash,
                        normalizedModule,
                        branchId,
                        mode
                )
                .ifPresent(existingJob -> {
                    if (
                            existingJob.getStatus()
                                    == ImportStatus.COMPLETED
                    ) {
                        throw new IllegalStateException(
                                "File has already been successfully imported. "
                                        + "Job ID: "
                                        + existingJob.getJobId()
                        );
                    }

                    if (isRunningStatus(
                            existingJob.getStatus()
                    )) {
                        throw new IllegalStateException(
                                "File is currently being processed. "
                                        + "Job ID: "
                                        + existingJob.getJobId()
                        );
                    }
                });

        String jobId =
                UUID.randomUUID()
                        .toString();

        ErpImportJob job =
                ErpImportJob.builder()
                        .jobId(jobId)
                        .module(normalizedModule)
                        .branchId(branchId)
                        .status(ImportStatus.CREATED)
                        .importMode(mode)
                        .fileHash(fileHash)
                        .uploadedFileName(uploadedFileName)
                        .startedAt(LocalDateTime.now())
                        .build();

        jobRepository.save(job);

        log.info(
                "Submitted Import Job {} for module {} using mode {}",
                jobId,
                normalizedModule,
                mode
        );

        startJobAsync(
                jobId,
                userId,
                plugin,
                safeImportOptions,
                safeTargetRows
        );

        return jobId;
    }

    private boolean isRunningStatus(
            ImportStatus status
    ) {
        return status == ImportStatus.INITIALIZING
                || status == ImportStatus.READING_ROWS
                || status == ImportStatus.VALIDATING_ROWS
                || status == ImportStatus.SAVING_BATCH;
    }

    private <DTO> void startJobAsync(
            String jobId,
            String userId,
            ImportPlugin<DTO> plugin,
            Map<String, Object> importOptions,
            Set<Integer> targetRowNumbers
    ) {
        executorService.submit(() -> {
            Path filePath = null;

            try {
                ErpImportJob job =
                        jobRepository.findById(jobId)
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "Import job was not found."
                                        )
                                );

                job.setStatus(
                        ImportStatus.INITIALIZING
                );

                jobRepository.save(job);

                filePath =
                        resolveTemporaryFile(job);

                if (!Files.isRegularFile(filePath)) {
                    markFileMissing(job);
                    return;
                }

                ImportContext context =
                        buildContext(
                                job,
                                userId,
                                plugin,
                                importOptions,
                                targetRowNumbers
                        );

                ImportSession session =
                        buildSession(jobId);

                ImportTemplate template =
                        plugin.getTemplate();

                if (template == null) {
                    throw new IllegalStateException(
                            "Import template configuration is missing "
                                    + "for module "
                                    + job.getModule()
                                    + "."
                    );
                }

                engineCoordinator.executeJob(
                        context,
                        session,
                        plugin,
                        filePath,
                        template
                );

                generateCorrectedWorkbookIfRequired(
                        jobId,
                        filePath,
                        template,
                        session
                );

                updateCompletedJob(
                        jobId,
                        session
                );
            } catch (Exception exception) {
                log.error(
                        "Fatal error in Import Job {}",
                        jobId,
                        exception
                );

                markJobFailed(
                        jobId,
                        exception
                );
            } finally {
                deleteTemporaryFile(filePath);
            }
        });
    }

    private Path resolveTemporaryFile(
            ErpImportJob job
    ) {
        Path temporaryDirectory =
                Path.of(
                                System.getProperty(
                                        "java.io.tmpdir"
                                )
                        )
                        .toAbsolutePath()
                        .normalize();

        Path filePath =
                temporaryDirectory
                        .resolve(
                                job.getUploadedFileName()
                        )
                        .normalize();

        if (!filePath.startsWith(
                temporaryDirectory
        )) {
            throw new SecurityException(
                    "Uploaded import filename produced an invalid path."
            );
        }

        return filePath;
    }

    private void markFileMissing(
            ErpImportJob job
    ) {
        log.error(
                "Import file was not found for job {}",
                job.getJobId()
        );

        job.setStatus(
                ImportStatus.FAILED
        );

        job.setLastCheckpoint(
                "Import file was not found."
        );

        job.setCompletedAt(
                LocalDateTime.now()
        );

        jobRepository.save(job);
    }

    private <DTO> ImportContext buildContext(
            ErpImportJob job,
            String userId,
            ImportPlugin<DTO> plugin,
            Map<String, Object> importOptions,
            Set<Integer> targetRowNumbers
    ) {
        return ImportContext.builder()
                .jobId(job.getJobId())
                .branchId(job.getBranchId())
                .userId(userId)
                .locale("en")
                .timeZone(
                        ZoneId.systemDefault()
                )
                .importMode(
                        job.getImportMode()
                )
                .chunkSize(
                        plugin.getManifest()
                                .getDefaultChunkSize()
                )
                .fileHash(
                        job.getFileHash()
                )
                .uploadedFileName(
                        job.getUploadedFileName()
                )
                .startTime(
                        System.currentTimeMillis()
                )
                .targetRowNumbers(
                        targetRowNumbers
                )
                .importOptions(
                        new ConcurrentHashMap<>(
                                importOptions
                        )
                )
                .build();
    }

    private ImportSession buildSession(
            String jobId
    ) {
        return ImportSession.builder()
                .jobId(jobId)
                .currentChunk(0)
                .processedRows(0)
                .successRows(0)
                .failedRows(0)
                .startTime(
                        System.currentTimeMillis()
                )
                .currentLifecycle(
                        ImportStatus.INITIALIZING
                )
                .build();
    }

    private void generateCorrectedWorkbookIfRequired(
            String jobId,
            Path filePath,
            ImportTemplate template,
            ImportSession session
    ) {
        if (
                session.getFailedRows() <= 0
                        || filePath == null
                        || !Files.isRegularFile(filePath)
        ) {
            return;
        }

        try {
            Path correctedWorkbook =
                    correctedWorkbookService
                            .generateCorrectedWorkbook(
                                    jobId,
                                    filePath,
                                    template
                            );

            if (correctedWorkbook != null) {
                log.info(
                        "Corrected workbook generated for import job {}: {}",
                        jobId,
                        correctedWorkbook.getFileName()
                );
            }
        } catch (Exception exception) {
            /*
             * Successful rows may already have committed independently.
             * A report-generation failure must not mark the completed
             * database import as failed.
             */
            log.error(
                    "Import job {} completed with row errors, but corrected "
                            + "workbook generation failed.",
                    jobId,
                    exception
            );
        }
    }

    private void updateCompletedJob(
            String jobId,
            ImportSession session
    ) {
        ErpImportJob completedJob =
                jobRepository.findById(jobId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Completed import job was not found."
                                )
                        );

        completedJob.setStatus(
                session.getCurrentLifecycle()
        );

        completedJob.setProcessedRows(
                session.getProcessedRows()
        );

        completedJob.setSuccessRows(
                session.getSuccessRows()
        );

        completedJob.setFailedRows(
                session.getFailedRows()
        );

        completedJob.setCompletedAt(
                LocalDateTime.now()
        );

        jobRepository.save(completedJob);
    }

    private Set<Integer> normalizeTargetRows(
            ImportMode mode,
            Set<Integer> targetRowNumbers
    ) {
        if (mode != ImportMode.RETRY_FAILED_ROWS) {
            if (
                    targetRowNumbers != null
                            && !targetRowNumbers.isEmpty()
            ) {
                throw new IllegalArgumentException(
                        "Target row numbers can be used only "
                                + "for Retry Failed Rows."
                );
            }

            return null;
        }

        if (
                targetRowNumbers == null
                        || targetRowNumbers.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "No failed Excel rows were found for retry."
            );
        }

        for (Integer rowNumber : targetRowNumbers) {
            if (
                    rowNumber == null
                            || rowNumber <= 1
            ) {
                throw new IllegalArgumentException(
                        "Retry row numbers must reference "
                                + "physical Excel data rows."
                );
            }
        }

        return Set.copyOf(
                targetRowNumbers
        );
    }

    private void validateSubmission(
            String module,
            String branchId,
            String userId,
            ImportMode mode,
            String fileHash,
            String uploadedFileName
    ) {
        if (
                module == null
                        || module.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Import module is required."
            );
        }

        validatePositiveInteger(
                branchId,
                "Import branch ID"
        );

        validatePositiveInteger(
                userId,
                "Import user ID"
        );

        if (mode == null) {
            throw new IllegalArgumentException(
                    "Import mode is required."
            );
        }

        if (
                fileHash == null
                        || fileHash.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Import file hash is required."
            );
        }

        if (
                uploadedFileName == null
                        || uploadedFileName.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Uploaded import filename is required."
            );
        }

        if (
                uploadedFileName.contains("/")
                        || uploadedFileName.contains("\\")
                        || uploadedFileName.contains("..")
        ) {
            throw new IllegalArgumentException(
                    "Uploaded import filename is invalid."
            );
        }
    }

    private void validatePositiveInteger(
            String value,
            String label
    ) {
        try {
            int parsed =
                    Integer.parseInt(value);

            if (parsed <= 0) {
                throw new NumberFormatException();
            }
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    label + " is invalid."
            );
        }
    }

    private void markJobFailed(
            String jobId,
            Exception exception
    ) {
        try {
            ErpImportJob job =
                    jobRepository.findById(jobId)
                            .orElseThrow();

            job.setStatus(
                    ImportStatus.FAILED
            );

            job.setLastCheckpoint(
                    "ERROR: "
                            + buildSafeFailureMessage(
                            exception
                    )
            );

            job.setCompletedAt(
                    LocalDateTime.now()
            );

            jobRepository.save(job);
        } catch (Exception updateException) {
            log.error(
                    "Could not update Import Job {} to FAILED.",
                    jobId,
                    updateException
            );
        }
    }

    private void deleteTemporaryFile(
            Path filePath
    ) {
        if (filePath == null) {
            return;
        }

        try {
            Files.deleteIfExists(
                    filePath
            );
        } catch (Exception exception) {
            log.warn(
                    "Failed to delete temporary import file for path {}",
                    filePath.getFileName(),
                    exception
            );
        }
    }

    private String buildSafeFailureMessage(
            Exception exception
    ) {
        String message =
                exception.getMessage();

        if (
                message == null
                        || message.isBlank()
        ) {
            message =
                    exception.getClass()
                            .getSimpleName();
        }

        return message.substring(
                0,
                Math.min(
                        message.length(),
                        490
                )
        );
    }
}