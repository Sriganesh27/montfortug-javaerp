package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpScholarshipApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpScholarshipApplicationRepository extends JpaRepository<ErpScholarshipApplication, Long> {
}