package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.ScholarshipApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScholarshipApplicationRepository extends JpaRepository<ScholarshipApplication, Integer> {
    List<ScholarshipApplication> findByStatus(String status);
    List<ScholarshipApplication> findByBranchIdAndStatus(Integer branchId, String status);
}