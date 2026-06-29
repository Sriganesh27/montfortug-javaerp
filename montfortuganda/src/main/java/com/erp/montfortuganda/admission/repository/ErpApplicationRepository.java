package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ErpApplicationRepository extends JpaRepository<ErpApplication, Long> {

    Optional<ErpApplication> findByApplicationNo(String applicationNo);

    // Optimized count query for Sequence Generation
    @Query("SELECT COUNT(a) FROM ErpApplication a WHERE a.branch.branchId = :branchId AND a.academicYearId = :academicYearId")
    long countByBranchAndYear(@Param("branchId") Integer branchId, @Param("academicYearId") Long academicYearId);

    // Used for listing on the admin dashboard
    List<ErpApplication> findByBranch_BranchIdOrderByCreatedAtDesc(Integer branchId);
}