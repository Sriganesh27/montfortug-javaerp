package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for public and branch-protected admission application access.
 *
 * <p>Branch-facing detail and update methods always include the authenticated
 * branch ID so an application from another branch cannot be returned merely
 * by guessing its database identifier.</p>
 */
@Repository
public interface ErpApplicationRepository
        extends JpaRepository<ErpApplication, Long> {

    // ---------------------------------------------------------------------
    // Existing public-application access
    // ---------------------------------------------------------------------

    Optional<ErpApplication> findByApplicationNo(
            String applicationNo
    );

    Optional<ErpApplication>
    findByApplicationNoAndDateOfBirth(
            String applicationNo,
            LocalDate dateOfBirth
    );

    boolean existsByApplicationNo(
            String applicationNo
    );

    // ---------------------------------------------------------------------
    // Existing dashboard counts
    // ---------------------------------------------------------------------

    @Query("""
            SELECT COUNT(application)
            FROM ErpApplication application
            WHERE application.branch.branchId = :branchId
              AND application.academicYearId = :academicYearId
            """)
    long countApplicationsByBranchAndAcademicYear(
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId
    );

    long countByBranch_BranchId(
            Integer branchId
    );

    long countByBranch_BranchIdAndApplicationStatus(
            Integer branchId,
            ErpApplication.ApplicationStatus applicationStatus
    );

    // ---------------------------------------------------------------------
    // Existing branch application list
    // ---------------------------------------------------------------------

    Page<ErpApplication> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Branch-scoped list query with the branch fetched in the same query.
     * This avoids lazy-loading failures when mapping summary responses.
     */
    @Query(
            value = """
                    SELECT application
                    FROM ErpApplication application
                    JOIN FETCH application.branch branch
                    WHERE branch.branchId = :branchId
                      AND application.status = 1
                    """,
            countQuery = """
                    SELECT COUNT(application)
                    FROM ErpApplication application
                    WHERE application.branch.branchId = :branchId
                      AND application.status = 1
                    """
    )
    Page<ErpApplication> findActiveByBranchId(
            @Param("branchId") Integer branchId,
            Pageable pageable
    );

    // ---------------------------------------------------------------------
    // Branch-safe application details
    // ---------------------------------------------------------------------

    /**
     * Loads one active application only when it belongs to the supplied
     * authenticated branch.
     */
    @Query("""
            SELECT application
            FROM ErpApplication application
            JOIN FETCH application.branch branch
            WHERE application.applicationId = :applicationId
              AND branch.branchId = :branchId
              AND application.status = 1
            """)
    Optional<ErpApplication> findActiveBranchApplication(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Integer branchId
    );

    /**
     * Locks one active branch application before a workflow transition.
     * Services must use this method for verification, test, fee, scholarship,
     * payment, and final-admission decisions that update the application.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT application
            FROM ErpApplication application
            JOIN FETCH application.branch branch
            WHERE application.applicationId = :applicationId
              AND branch.branchId = :branchId
              AND application.status = 1
            """)
    Optional<ErpApplication> findActiveBranchApplicationForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Integer branchId
    );

    boolean existsByApplicationIdAndBranch_BranchIdAndStatus(
            Long applicationId,
            Integer branchId,
            Integer status
    );
}
