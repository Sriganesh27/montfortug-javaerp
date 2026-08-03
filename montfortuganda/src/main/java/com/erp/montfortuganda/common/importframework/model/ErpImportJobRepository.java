package com.erp.montfortuganda.common.importframework.model;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpImportJobRepository
        extends JpaRepository<ErpImportJob, String> {

    /**
     * Used for import-file idempotency checks.
     */
    Optional<ErpImportJob>
    findFirstByFileHashAndModuleAndBranchIdAndImportMode(
            String fileHash,
            String module,
            String branchId,
            ImportMode importMode
    );

    /**
     * Returns a job only when it belongs to the authenticated branch.
     */
    Optional<ErpImportJob> findByJobIdAndBranchId(
            String jobId,
            String branchId
    );

    /**
     * Returns recent jobs for one module and authenticated branch only.
     */
    List<ErpImportJob>
    findTop50ByModuleAndBranchIdOrderByStartedAtDesc(
            String module,
            String branchId
    );

    /**
     * Existing progress update retained for backward compatibility while
     * EngineCoordinator is migrated to publish total rows.
     */
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query(
            """
            UPDATE ErpImportJob job
               SET job.status = :status,
                   job.processedRows = :processed,
                   job.successRows = :success,
                   job.failedRows = :failed
             WHERE job.jobId = :jobId
            """
    )
    int updateProgress(
            @Param("jobId")
            String jobId,

            @Param("status")
            ImportStatus status,

            @Param("processed")
            int processed,

            @Param("success")
            int success,

            @Param("failed")
            int failed
    );

    /**
     * Atomically publishes the complete frontend progress snapshot,
     * including the exact number of rows selected for the job.
     */
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query(
            """
            UPDATE ErpImportJob job
               SET job.status = :status,
                   job.totalRows = :total,
                   job.processedRows = :processed,
                   job.successRows = :success,
                   job.failedRows = :failed
             WHERE job.jobId = :jobId
            """
    )
    int updateProgressWithTotal(
            @Param("jobId")
            String jobId,

            @Param("status")
            ImportStatus status,

            @Param("total")
            int total,

            @Param("processed")
            int processed,

            @Param("success")
            int success,

            @Param("failed")
            int failed
    );

    /**
     * Stores a safe job-level failure reason in last_checkpoint.
     */
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query(
            """
            UPDATE ErpImportJob job
               SET job.status = :status,
                   job.totalRows = :total,
                   job.processedRows = :processed,
                   job.successRows = :success,
                   job.failedRows = :failed,
                   job.lastCheckpoint = :failureReason
             WHERE job.jobId = :jobId
            """
    )
    int updateFailedProgress(
            @Param("jobId")
            String jobId,

            @Param("status")
            ImportStatus status,

            @Param("total")
            int total,

            @Param("processed")
            int processed,

            @Param("success")
            int success,

            @Param("failed")
            int failed,

            @Param("failureReason")
            String failureReason
    );

}
