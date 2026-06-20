package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpScholarshipAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpScholarshipAllocationRepository extends JpaRepository<ErpScholarshipAllocation, Long> {
}