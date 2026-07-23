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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class ImportFacade {

    private final PluginRegistry pluginRegistry;
    private final ErpImportJobRepository jobRepository;
    private final EngineCoordinator engineCoordinator;
    private final ExecutorService executorService;

    public ImportFacade(
            PluginRegistry pluginRegistry,
            ErpImportJobRepository jobRepository,
            EngineCoordinator engineCoordinator,
            @Qualifier("importVirtualThreadExecutor")
            ExecutorService executorService
    ) {
        this.pluginRegistry = pluginRegistry;
        this.jobRepository = jobRepository;
        this.engineCoordinator = engineCoordinator;
        this.executorService = executorService;
    }



    /**
     * Submits an import job with trusted module-specific options.
     *
     * <p>The options must be created by the backend controller after
     * authentication. Identity and branch values must never be copied from
     * browser-controlled request parameters.</p>
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
        validateSubmission(
                module,
                branchId,
                userId,
                mode,
                fileHash,
                uploadedFileName
        );

        ImportPlugin<?> plugin =
                pluginRegistry.getPlugin(module);

        if (
                !plugin.getManifest()
                        .getSupportedImportModes()
                        .contains(mode)
        ) {
            throw new IllegalArgumentException(
                    "Import mode "
                            + mode
                            + " is not supported for module "
                            + module
                            + "."
            );
        }

        /*
         * Copy the options before starting asynchronous work.
         * The worker receives its own map and cannot observe later mutations.
         */
        Map<String, Object> safeImportOptions =
                importOptions == null
                        ? Map.of()
                        : Map.copyOf(importOptions);

        jobRepository
                .findFirstByFileHashAndModuleAndBranchIdAndImportMode(
                        fileHash,
                        module,
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

                    if (
                            existingJob.getStatus()
                                    == ImportStatus.INITIALIZING
                                    || existingJob.getStatus()
                                    == ImportStatus.READING_ROWS
                                    || existingJob.getStatus()
                                    == ImportStatus.VALIDATING_ROWS
                                    || existingJob.getStatus()
                                    == ImportStatus.SAVING_BATCH
                    ) {
                        throw new IllegalStateException(
                                "File is currently being processed. "
                                        + "Job ID: "
                                        + existingJob.getJobId()
                        );
                    }
                });

        String normalizedModule =
                module.trim()
                        .toUpperCase();

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
                "Submitted Import Job {} for module {}",
                jobId,
                normalizedModule
        );

        startJobAsync(
                jobId,
                userId,
                plugin,
                safeImportOptions
        );

        return jobId;
    }

    private <DTO> void startJobAsync(
            String jobId,
            String userId,
            ImportPlugin<DTO> plugin,
            Map<String, Object> importOptions
    ) {
        executorService.submit(() -> {
            Path filePath = null;

            try {
                ErpImportJob job =
                        jobRepository
                                .findById(jobId)
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
                        Path.of(
                                        System.getProperty(
                                                "java.io.tmpdir"
                                        ),
                                        job.getUploadedFileName()
                                )
                                .toAbsolutePath()
                                .normalize();

                if (!Files.exists(filePath)) {
                    log.error(
                            "Import file was not found for job {}",
                            jobId
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
                    return;
                }

                ImportContext context =
                        ImportContext.builder()
                                .jobId(jobId)
                                .branchId(
                                        job.getBranchId()
                                )
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
                                .targetRowNumbers(null)
                                .importOptions(
                                        new ConcurrentHashMap<>(
                                                importOptions
                                        )
                                )
                                .build();

                ImportSession session =
                        ImportSession.builder()
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

                ImportTemplate template =
                        ImportTemplate.builder()
                                .build();

                engineCoordinator.executeJob(
                        context,
                        session,
                        plugin,
                        filePath,
                        template
                );

                ErpImportJob completedJob =
                        jobRepository
                                .findById(jobId)
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

                jobRepository.save(
                        completedJob
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
                deleteTemporaryFile(
                        filePath
                );
            }
        });
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
    }

    private void validatePositiveInteger(
            String value,
            String label
    ) {
        try {
            int parsed =
                    Integer.parseInt(
                            value
                    );

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
                    jobRepository
                            .findById(jobId)
                            .orElseThrow();

            job.setStatus(
                    ImportStatus.FAILED
            );

            String safeMessage =
                    buildSafeFailureMessage(
                            exception
                    );

            job.setLastCheckpoint(
                    "ERROR: " + safeMessage
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