package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.ErpScholarshipApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpScholarshipApplicationRepository
        extends JpaRepository<ErpScholarshipApplication, Long> {

    Optional<ErpScholarshipApplication>
    findByApplication_ApplicationIdAndActiveTrue(
            Long applicationId
    );

    @Query("""
            select s
            from ErpScholarshipApplication s
            where s.scholarshipAppId = :scholarshipAppId
              and s.branchId = :branchId
              and s.active = true
            """)
    Optional<ErpScholarshipApplication>
    findActiveByScholarshipAppIdAndBranch(
            @Param("scholarshipAppId") Long scholarshipAppId,
            @Param("branchId") Long branchId
    );

    /**
     * Branch-scoped pessimistic lock used when a Scholarship record is
     * modified by the Branch workflow.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from ErpScholarshipApplication s
            where s.scholarshipAppId = :scholarshipAppId
              and s.branchId = :branchId
              and s.active = true
            """)
    Optional<ErpScholarshipApplication>
    findActiveByScholarshipAppIdAndBranchForUpdate(
            @Param("scholarshipAppId") Long scholarshipAppId,
            @Param("branchId") Long branchId
    );

    /**
     * Super Admin workflow lookup. No branch from the authenticated browser
     * is trusted here; the scholarship record itself resolves its branch.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from ErpScholarshipApplication s
            where s.scholarshipAppId = :scholarshipAppId
              and s.active = true
            """)
    Optional<ErpScholarshipApplication>
    findActiveByScholarshipAppIdForUpdate(
            @Param("scholarshipAppId") Long scholarshipAppId
    );

    /**
     * Branch Scholarship list.
     *
     * submitted_at is a database column on erp_scholarship_applications,
     * but submittedAt is intentionally no longer a persistent property on
     * ErpScholarshipApplication because Scholarship History owns the
     * submitted snapshot. Therefore this must be a native query rather than
     * a derived Spring Data method.
     */
    @Query(value = """
            SELECT s.*
            FROM erp_scholarship_applications s
            WHERE s.branch_id = :branchId
              AND s.active = 1
            ORDER BY s.submitted_at DESC
            """,
            nativeQuery = true)
    List<ErpScholarshipApplication>
    findAllByBranchIdAndActiveTrueOrderBySubmittedAtDesc(
            @Param("branchId") Long branchId
    );

    Optional<ErpScholarshipApplication>
    findByPublicTokenHashAndActiveTrue(
            String publicTokenHash
    );

    Optional<ErpScholarshipApplication>
    findBySchoolAccessTokenHashAndActiveTrue(
            String schoolAccessTokenHash
    );

    @Query("""
            select s
            from ErpScholarshipApplication s
            where s.application.applicationId = :applicationId
              and s.branchId = :branchId
              and s.active = true
            """)
    Optional<ErpScholarshipApplication>
    findActiveByApplicationAndBranch(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Long branchId
    );

    /**
     * Resolves the scholarship application for one admission application,
     * academic year and term. A later cycle must never reuse an older
     * scholarship record.
     *
     * The term is stored in Scholarship History, which is the immutable
     * snapshot/history layer for scholarship request data.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from ErpScholarshipApplication s
            where s.application.applicationId = :applicationId
              and s.branchId = :branchId
              and s.academicYear = :academicYear
              and s.active = true
              and exists (
                    select h.scholarshipHistoryId
                    from ErpScholarshipHistory h
                    where h.scholarshipApplication = s
                      and h.termRequested = :term
              )
            order by s.scholarshipAppId desc
            """)
    Optional<ErpScholarshipApplication>
    findActiveByApplicationBranchAcademicYearAndTermForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Long branchId,
            @Param("academicYear") String academicYear,
            @Param("term") String term
    );

    /**
     * Scholarship Overview query.
     *
     * This remains native because the current Overview implementation uses
     * the database's submitted_at column for ordering while the current
     * ErpScholarshipApplication entity does not expose submittedAt.
     */
    @Query(value = """
            SELECT s.*
            FROM erp_scholarship_applications s
            INNER JOIN erp_applications a
                ON a.application_id = s.application_id
            INNER JOIN erp_classes c
                ON c.class_id = a.branch_class_id
            WHERE s.branch_id = :branchId
              AND s.active = 1
              AND (:academicYear IS NULL OR s.academic_year = :academicYear)
              AND (
                    :term IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM erp_academic_terms t
                        INNER JOIN erp_academic_years ay
                            ON ay.academic_year_id = t.academic_year_id
                        WHERE ay.academic_year_code = :academicYear
                          AND ay.branch_id = :branchId
                          AND t.active = 1
                          AND (
                                LOWER(TRIM(t.term_code)) =
                                LOWER(TRIM(:term))
                                OR LOWER(TRIM(t.term_name)) =
                                LOWER(TRIM(:term))
                          )
                          AND LOWER(TRIM(s.term_requested)) =
                              LOWER(TRIM(t.term_name))
                    )
              )
              AND (
                    :classId IS NULL
                    OR a.branch_class_id = :classId
              )
              AND (
                    :levelId IS NULL
                    OR c.level_id = :levelId
              )
            ORDER BY s.submitted_at DESC
            """,
            nativeQuery = true)
    List<ErpScholarshipApplication>
    findActiveOverviewApplications(
            @Param("branchId") Long branchId,
            @Param("academicYear") String academicYear,
            @Param("term") String term,
            @Param("classId") Integer classId,
            @Param("levelId") Integer levelId
    );

    default List<ErpScholarshipApplication>
    findActiveOverviewApplications(
            Long branchId,
            String academicYear,
            String term,
            Integer classId
    ) {
        return findActiveOverviewApplications(
                branchId,
                academicYear,
                term,
                classId,
                null
        );
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from ErpScholarshipApplication s
            where s.application.applicationId = :applicationId
              and s.branchId = :branchId
              and s.active = true
            """)
    Optional<ErpScholarshipApplication>
    findActiveByApplicationAndBranchForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Long branchId
    );

    /**
     * Branch-scoped locked lookup used by Fee Discussion when synchronizing
     * the Scholarship Application master record.
     *
     * The application_id column is unique, so an existing inactive
     * Scholarship Application must be reactivated rather than inserting a
     * second row for the same admission application.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from ErpScholarshipApplication s
            where s.application.applicationId = :applicationId
              and s.branchId = :branchId
            """)
    Optional<ErpScholarshipApplication>
    findByApplicationAndBranchForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Long branchId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from ErpScholarshipApplication s
            where s.publicTokenHash = :tokenHash
              and s.active = true
            """)
    Optional<ErpScholarshipApplication>
    findActiveByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from ErpScholarshipApplication s
            where s.schoolAccessTokenHash = :tokenHash
              and s.active = true
            """)
    Optional<ErpScholarshipApplication>
    findActiveBySchoolAccessTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );
}
