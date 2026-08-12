package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplicationInterview;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for the one-to-one Entrance Test / interview record attached to
 * an admission application.
 *
 * <p>Branch-facing queries always include the authenticated branch ID so an
 * interview from another branch cannot be read or modified by guessing an
 * application ID.</p>
 */
@Repository
public interface ErpApplicationInterviewRepository
        extends JpaRepository<ErpApplicationInterview, Long> {

    /**
     * Finds the active Entrance Test record for an application.
     */
    Optional<ErpApplicationInterview>
    findByApplication_ApplicationIdAndActiveTrue(
            Long applicationId
    );

    /**
     * Checks whether an active Entrance Test record already exists for the
     * application. The database also enforces one record per application.
     */
    boolean existsByApplication_ApplicationIdAndActiveTrue(
            Long applicationId
    );

    /**
     * Branch-safe read for the Entrance Test record.
     */
    @Query("""
            SELECT interview
            FROM ErpApplicationInterview interview
            JOIN FETCH interview.application application
            JOIN FETCH application.branch branch
            WHERE application.applicationId = :applicationId
              AND branch.branchId = :branchId
              AND interview.active = true
              AND application.status = 1
            """)
    Optional<ErpApplicationInterview> findActiveByApplicationAndBranch(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Integer branchId
    );

    /**
     * Branch-safe pessimistic lock used by schedule/start/complete operations.
     * This prevents two concurrent requests from updating the same test record
     * at the same time.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT interview
            FROM ErpApplicationInterview interview
            JOIN FETCH interview.application application
            JOIN FETCH application.branch branch
            WHERE application.applicationId = :applicationId
              AND branch.branchId = :branchId
              AND interview.active = true
              AND application.status = 1
            """)
    Optional<ErpApplicationInterview> findActiveByApplicationAndBranchForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Integer branchId
    );

    /**
     * Direct lock by interview ID while still enforcing Branch ownership.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT interview
            FROM ErpApplicationInterview interview
            JOIN FETCH interview.application application
            JOIN FETCH application.branch branch
            WHERE interview.interviewId = :interviewId
              AND branch.branchId = :branchId
              AND interview.active = true
              AND application.status = 1
            """)
    Optional<ErpApplicationInterview> findActiveByIdAndBranchForUpdate(
            @Param("interviewId") Long interviewId,
            @Param("branchId") Integer branchId
    );
}
