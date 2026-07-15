package com.erp.montfortuganda.common.importframework.engine;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.context.ImportSession;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import com.erp.montfortuganda.common.importframework.model.ErpImportJob;
import com.erp.montfortuganda.common.importframework.model.ErpImportJobRepository;
import com.erp.montfortuganda.common.importframework.registry.PluginRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class ImportFacade {

    private final PluginRegistry pluginRegistry;
    private final ErpImportJobRepository jobRepository;
    private final EngineCoordinator engineCoordinator;
    private final ExecutorService executorService;

    public ImportFacade(PluginRegistry pluginRegistry,
                        ErpImportJobRepository jobRepository,
                        EngineCoordinator engineCoordinator,
                        @Qualifier("importVirtualThreadExecutor") ExecutorService executorService) {
        this.pluginRegistry = pluginRegistry;
        this.jobRepository = jobRepository;
        this.engineCoordinator = engineCoordinator;
        this.executorService = executorService;
    }

    public String submitImportJob(String module, String branchId, String userId,
                                  ImportMode mode, String fileHash, String uploadedFileName) {

        var plugin = pluginRegistry.getPlugin(module);

        if (!plugin.getManifest().getSupportedImportModes().contains(mode)) {
            throw new IllegalArgumentException("Import mode " + mode + " is not supported for module " + module);
        }

        // IDEMPOTENCY CHECK
        jobRepository.findFirstByFileHashAndModuleAndBranchIdAndImportMode(fileHash, module, branchId, mode)
                .ifPresent(existingJob -> {
                    if (existingJob.getStatus() == ImportStatus.COMPLETED) {
                        throw new RuntimeException("File has already been successfully imported. Job ID: " + existingJob.getJobId());
                    }
                    if (existingJob.getStatus() == ImportStatus.INITIALIZING || existingJob.getStatus() == ImportStatus.READING_ROWS) {
                        throw new RuntimeException("File is currently being processed. Job ID: " + existingJob.getJobId());
                    }
                });

        String jobId = UUID.randomUUID().toString();

        ErpImportJob job = ErpImportJob.builder()
                .jobId(jobId)
                .module(module)
                .branchId(branchId)
                .status(ImportStatus.CREATED)
                .importMode(mode)
                .fileHash(fileHash)
                .uploadedFileName(uploadedFileName)
                .startedAt(LocalDateTime.now())
                .build();

        jobRepository.save(job);
        log.info("Submitted new Import Job {} for module {}", jobId, module);

        startJobAsync(jobId, userId, plugin);

        return jobId;
    }

    private <DTO> void startJobAsync(String jobId, String userId,
                                     com.erp.montfortuganda.common.importframework.plugin.ImportPlugin<DTO> plugin) {

        executorService.submit(() -> {
            Path filePath = null;
            try {
                ErpImportJob job = jobRepository.findById(jobId).orElseThrow();
                job.setStatus(ImportStatus.INITIALIZING);
                jobRepository.save(job);

                // Read the EXACT filename stored in the job entity (Saved by the Controller)
                filePath = Path.of(System.getProperty("java.io.tmpdir"), job.getUploadedFileName());

                if (!java.nio.file.Files.exists(filePath)) {
                    log.error("Import file not found at path: {}", filePath);
                    job.setStatus(ImportStatus.FAILED);
                    job.setLastCheckpoint("File not found: " + filePath);
                    job.setCompletedAt(LocalDateTime.now());
                    jobRepository.save(job);
                    return;
                }

                ImportContext context = ImportContext.builder()
                        .jobId(jobId)
                        .branchId(job.getBranchId())
                        .userId(userId)
                        .locale("en")
                        .timeZone(ZoneId.systemDefault())
                        .importMode(job.getImportMode())
                        .chunkSize(plugin.getManifest().getDefaultChunkSize())
                        .fileHash(job.getFileHash())
                        .uploadedFileName(job.getUploadedFileName())
                        .startTime(System.currentTimeMillis())
                        .targetRowNumbers(null)
                        .build();

                ImportSession session = ImportSession.builder()
                        .jobId(jobId)
                        .currentChunk(0)
                        .processedRows(0)
                        .successRows(0)
                        .failedRows(0)
                        .startTime(System.currentTimeMillis())
                        .currentLifecycle(ImportStatus.INITIALIZING)
                        .build();

                com.erp.montfortuganda.common.importframework.registry.ImportTemplate template =
                        com.erp.montfortuganda.common.importframework.registry.ImportTemplate.builder().build();

                // Start Processing
                engineCoordinator.executeJob(context, session, plugin, filePath, template);

                // Mark Completed
                ErpImportJob completedJob = jobRepository.findById(jobId).orElseThrow();
                completedJob.setStatus(session.getCurrentLifecycle());
                completedJob.setProcessedRows(session.getProcessedRows());
                completedJob.setSuccessRows(session.getSuccessRows());
                completedJob.setFailedRows(session.getFailedRows());
                completedJob.setCompletedAt(LocalDateTime.now());
                jobRepository.save(completedJob);

            } catch (Exception e) {
                log.error("Fatal error in import job {}: {}", jobId, e.getMessage(), e);
                try {
                    ErpImportJob job = jobRepository.findById(jobId).orElseThrow();
                    job.setStatus(ImportStatus.FAILED);
                    String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                    job.setLastCheckpoint("ERROR: " + errorMsg.substring(0, Math.min(errorMsg.length(), 490)));
                    job.setCompletedAt(LocalDateTime.now());
                    jobRepository.save(job);
                } catch (Exception inner) {
                    log.error("Could not update job status to FAILED", inner);
                }
            } finally {
                // Guaranteed Cleanup
                if (filePath != null) {
                    try { java.nio.file.Files.deleteIfExists(filePath); }
                    catch (Exception e) { log.warn("Failed to delete temp file {}", filePath, e); }
                }
            }
        });
    }
}