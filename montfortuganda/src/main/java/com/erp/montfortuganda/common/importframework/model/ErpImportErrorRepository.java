package com.erp.montfortuganda.common.importframework.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ErpImportErrorRepository
        extends JpaRepository<ErpImportError, Long> {

    boolean existsByJobId(String jobId);

    List<ErpImportError> findByJobId(
            String jobId
    );

    Page<ErpImportError> findByJobId(
            String jobId,
            Pageable pageable
    );

    /**
     * Returns only unique physical Excel row numbers that failed.
     * Header and invalid framework rows are excluded.
     */
    @Query(
            """
            SELECT DISTINCT error.rowNumber
              FROM ErpImportError error
             WHERE error.jobId = :jobId
               AND error.rowNumber > 1
             ORDER BY error.rowNumber
            """
    )
    Set<Integer> findDistinctFailedRowNumbers(
            @Param("jobId")
            String jobId
    );
}