package com.erp.montfortuganda.common.importframework.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErpImportErrorRepository
        extends JpaRepository<ErpImportError, Long> {

    List<ErpImportError> findByJobId(
            String jobId
    );

    Page<ErpImportError> findByJobId(
            String jobId,
            Pageable pageable
    );
}