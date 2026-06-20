package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpBranchFundAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpBranchFundAllocationRepository extends JpaRepository<ErpBranchFundAllocation, Long> {
}