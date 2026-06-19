package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.ScholarshipBranchAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScholarshipBranchAllocationRepository extends JpaRepository<ScholarshipBranchAllocation, Integer> {
    List<ScholarshipBranchAllocation> findByBranchIdOrderByCreatedAtDesc(Integer branchId);
}