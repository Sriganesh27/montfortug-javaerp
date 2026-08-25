package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplicationFee;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for application-level fee discussion and payment summary data.
 *
 * <p>All branch-facing reads and updates verify the application's branch
 * ownership on the server.</p>
 */
@Repository
public interface ErpApplicationFeeRepository
        extends JpaRepository<ErpApplicationFee, Long> {

    /**
     * Finds the active fee record for an application.
     */
    Optional<ErpApplicationFee>
    findByApplication_ApplicationIdAndActiveTrue(
            Long applicationId
    );

    /**
     * Checks whether an active fee record already exists.
     */
    boolean existsByApplication_ApplicationIdAndActiveTrue(
            Long applicationId
    );

    /**
     * Branch-safe read of the application's fee record.
     */
    @Query("""
            SELECT fee
            FROM ErpApplicationFee fee
            JOIN FETCH fee.application application
            JOIN FETCH application.branch branch
            WHERE application.applicationId = :applicationId
              AND branch.branchId = :branchId
              AND fee.active = true
              AND application.status = 1
            """)
    Optional<ErpApplicationFee> findActiveByApplicationAndBranch(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Integer branchId
    );

    /**
     * Branch-safe pessimistic lock for fee discussion/payment changes.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT fee
            FROM ErpApplicationFee fee
            JOIN FETCH fee.application application
            JOIN FETCH application.branch branch
            WHERE application.applicationId = :applicationId
              AND branch.branchId = :branchId
              AND fee.active = true
              AND application.status = 1
            """)
    Optional<ErpApplicationFee>
    findActiveByApplicationAndBranchForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Integer branchId
    );
}
