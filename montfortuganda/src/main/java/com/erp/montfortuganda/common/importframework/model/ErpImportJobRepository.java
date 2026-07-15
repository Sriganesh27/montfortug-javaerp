package com.erp.montfortuganda.common.importframework.model;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpImportJobRepository extends JpaRepository<ErpImportJob, String> {

    Optional<ErpImportJob> findFirstByFileHashAndModuleAndBranchIdAndImportMode(
            String fileHash, String module, String branchId, ImportMode importMode);

    // Atomic Modifying Query to flush progress to the Database for frontend polling
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ErpImportJob j SET j.status = :status, j.processedRows = :processed, j.successRows = :success, j.failedRows = :failed WHERE j.jobId = :jobId")
    void updateProgress(@Param("jobId") String jobId,
                        @Param("status") ImportStatus status,
                        @Param("processed") int processed,
                        @Param("success") int success,
                        @Param("failed") int failed);
}