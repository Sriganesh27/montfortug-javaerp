package com.erp.montfortuganda.common.importframework.service;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import com.erp.montfortuganda.common.importframework.model.ErpImportJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Publishes import-job progress in short, independent ERP transactions.
 *
 * <p>The complete Excel import must not run inside one long database
 * transaction. Each progress snapshot therefore uses REQUIRES_NEW so it can
 * be committed safely from the asynchronous import thread, including when
 * the main import fails.</p>
 */
@Service
@RequiredArgsConstructor
public class ImportProgressTransactionService {

    private final ErpImportJobRepository jobRepository;

    /**
     * Existing progress update retained while EngineCoordinator is migrated
     * to publish total rows.
     */
    @Transactional(
            transactionManager = "erpTransactionManager",
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class
    )
    public void updateProgress(
            String jobId,
            ImportStatus status,
            int processedRows,
            int successRows,
            int failedRows
    ) {
        String normalizedJobId =
                requireJobId(jobId);

        ImportStatus requiredStatus =
                requireStatus(status);

        validateCounters(
                null,
                processedRows,
                successRows,
                failedRows
        );

        int updatedRows =
                jobRepository.updateProgress(
                        normalizedJobId,
                        requiredStatus,
                        processedRows,
                        successRows,
                        failedRows
                );

        ensureJobUpdated(
                normalizedJobId,
                updatedRows
        );
    }

    /**
     * Publishes the complete frontend progress snapshot.
     *
     * @param jobId         import job identifier
     * @param status        current import lifecycle
     * @param totalRows     total rows selected for this job
     * @param processedRows rows already validated or processed
     * @param successRows   successfully imported rows
     * @param failedRows    failed rows
     */
    @Transactional(
            transactionManager = "erpTransactionManager",
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class
    )
    public void updateProgress(
            String jobId,
            ImportStatus status,
            int totalRows,
            int processedRows,
            int successRows,
            int failedRows
    ) {
        String normalizedJobId =
                requireJobId(jobId);

        ImportStatus requiredStatus =
                requireStatus(status);

        validateCounters(
                totalRows,
                processedRows,
                successRows,
                failedRows
        );

        int updatedRows =
                jobRepository.updateProgressWithTotal(
                        normalizedJobId,
                        requiredStatus,
                        totalRows,
                        processedRows,
                        successRows,
                        failedRows
                );

        ensureJobUpdated(
                normalizedJobId,
                updatedRows
        );
    }
    /**
     * Publishes the failed state and a safe job-level failure reason.
     */
    @Transactional(
            transactionManager = "erpTransactionManager",
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class
    )
    public void updateFailure(
            String jobId,
            int totalRows,
            int processedRows,
            int successRows,
            int failedRows,
            String failureReason
    ) {
        String normalizedJobId =
                requireJobId(jobId);

        validateCounters(
                totalRows,
                processedRows,
                successRows,
                failedRows
        );

        String normalizedFailureReason =
                normalizeFailureReason(
                        failureReason
                );

        int updatedRows =
                jobRepository.updateFailedProgress(
                        normalizedJobId,
                        ImportStatus.FAILED,
                        totalRows,
                        processedRows,
                        successRows,
                        failedRows,
                        normalizedFailureReason
                );

        ensureJobUpdated(
                normalizedJobId,
                updatedRows
        );
    }

    private String normalizeFailureReason(
            String failureReason
    ) {
        String normalized =
                failureReason == null
                        ? ""
                        : failureReason.trim();

        if (normalized.isBlank()) {
            normalized =
                    "UNEXPECTED_ERROR:INTERNAL_IMPORT_ERROR: "
                            + "Import could not be completed.";
        }

        if (normalized.length() > 500) {
            return normalized.substring(
                    0,
                    500
            );
        }

        return normalized;
    }



    private String requireJobId(
            String jobId
    ) {
        if (
                jobId == null
                        || jobId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Import job ID is required."
            );
        }

        return jobId.trim();
    }

    private ImportStatus requireStatus(
            ImportStatus status
    ) {
        return Objects.requireNonNull(
                status,
                "Import status is required."
        );
    }

    private void validateCounters(
            Integer totalRows,
            int processedRows,
            int successRows,
            int failedRows
    ) {
        if (
                processedRows < 0
                        || successRows < 0
                        || failedRows < 0
        ) {
            throw new IllegalArgumentException(
                    "Import progress counters cannot be negative."
            );
        }

        if (
                totalRows != null
                        && totalRows < 0
        ) {
            throw new IllegalArgumentException(
                    "Import total rows cannot be negative."
            );
        }

        if (
                successRows + failedRows
                        > processedRows
        ) {
            throw new IllegalArgumentException(
                    "Successful and failed rows cannot exceed processed rows."
            );
        }

        if (
                totalRows != null
                        && processedRows > totalRows
        ) {
            throw new IllegalArgumentException(
                    "Processed rows cannot exceed total rows."
            );
        }
    }

    private void ensureJobUpdated(
            String jobId,
            int updatedRows
    ) {
        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "Import progress could not be updated for job: "
                            + jobId
            );
        }
    }
}