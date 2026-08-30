package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpBranchFundAllocation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    /**
     * Locks the complete branch Scholarship funding pool for one exact
     * academic year and term while a Scholarship allocation transaction
     * calculates and consumes available source balances.
     *
     * The enclosing service method is transactional, so the pessimistic
     * write lock remains held until the transaction commits or rolls back.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
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
