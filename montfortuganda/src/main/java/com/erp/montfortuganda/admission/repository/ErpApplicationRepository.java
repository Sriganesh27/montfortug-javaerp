package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Add this import

@Repository
public interface ErpApplicationRepository extends JpaRepository<ErpApplication, Long> {
    long countByAcademicYearAndBranchId(String academicYear, Long branchId);

    Optional<ErpApplication> findByRefNumber(String refNumber);
}