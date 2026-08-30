package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpScholarshipAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErpScholarshipAllocationRepository
        extends JpaRepository<ErpScholarshipAllocation, Long> {

    /**
     * Returns all Scholarship allocation transaction records for one branch.
     *
     * Branch scope is explicit and must always be supplied by branch-scoped
     * services. This repository never resolves a branch from request data.
     */
    List<ErpScholarshipAllocation> findAllByBranchId(
            Long branchId
    );

    /**
     * Returns all allocation transactions for one branch and academic year.
     */
    List<ErpScholarshipAllocation>
    findAllByBranchIdAndAcademicYear(
            Long branchId,
            String academicYear
    );

    /**
     * Returns all allocation transactions for one branch, academic year and
     * term. This is used for branch fund accounting and donor/source usage.
     */
    List<ErpScholarshipAllocation>
    findAllByBranchIdAndAcademicYearAndTerm(
            Long branchId,
            String academicYear,
            String term
    );

    /**
     * Returns all allocation transactions originating from one donation.
     *
     * Donation data itself belongs to the separate website database, so this
     * is intentionally only a local ERP allocation lookup. There is no
     * cross-database JPA relationship to web_donations.
     */
    List<ErpScholarshipAllocation> findAllByDonationId(
            Long donationId
    );

    /**
     * Returns every allocation transaction belonging to one exact
     * Scholarship History record.
     *
     * This is the authoritative query for History-level financial tracking:
     *
     *   approved amount
     *          -
     *   allocations linked to this history
     *          =
     *   remaining amount
     *
     * A student can have multiple Scholarship cycles, therefore queries that
     * only use student/year/term must not be used when the calculation is
     * specifically for one Scholarship History.
     */
    List<ErpScholarshipAllocation>
    findAllByScholarshipHistoryScholarshipHistoryId(
            Long scholarshipHistoryId
    );
}
