package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpBranchFundAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErpBranchFundAllocationRepository
        extends JpaRepository<ErpBranchFundAllocation, Long> {

    List<ErpBranchFundAllocation> findAllByBranchId(
            Long branchId
    );

    List<ErpBranchFundAllocation>
    findAllByBranchIdAndAcademicYear(
            Long branchId,
            String academicYear
    );

    List<ErpBranchFundAllocation>
    findAllByBranchIdAndAcademicYearAndTerm(
            Long branchId,
            String academicYear,
            String term
    );

    List<ErpBranchFundAllocation> findAllByDonationId(
            Long donationId
    );
}
