package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpScholarshipAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErpScholarshipAllocationRepository
        extends JpaRepository<ErpScholarshipAllocation, Long> {

    List<ErpScholarshipAllocation> findAllByBranchId(
            Long branchId
    );

    List<ErpScholarshipAllocation>
    findAllByBranchIdAndAcademicYear(
            Long branchId,
            String academicYear
    );

    List<ErpScholarshipAllocation>
    findAllByBranchIdAndAcademicYearAndTerm(
            Long branchId,
            String academicYear,
            String term
    );

    List<ErpScholarshipAllocation> findAllByDonationId(
            Long donationId
    );
}
