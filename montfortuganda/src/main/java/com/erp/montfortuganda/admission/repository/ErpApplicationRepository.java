package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpApplicationRepository extends JpaRepository<ErpApplication, Long> {
    long countByAcademicYearAndBranchId(String academicYear, Long branchId);
}