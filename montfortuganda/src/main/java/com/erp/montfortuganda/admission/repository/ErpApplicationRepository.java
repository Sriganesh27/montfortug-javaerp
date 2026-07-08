package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ErpApplicationRepository extends JpaRepository<ErpApplication, Long> {

    Optional<ErpApplication> findByApplicationNo(String applicationNo);

    @Query("SELECT COUNT(a) FROM ErpApplication a WHERE a.branch.branchId = :branchId AND a.academicYearId = :academicYearId")
    long countApplicationsByBranchAndAcademicYear(@Param("branchId") Integer branchId, @Param("academicYearId") Long academicYearId);

    long countByBranch_BranchId(Integer branchId);

    long countByBranch_BranchIdAndApplicationStatus(Integer branchId, ErpApplication.ApplicationStatus applicationStatus);

    Page<ErpApplication> findByBranch_BranchId(Integer branchId, Pageable pageable);
}