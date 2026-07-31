package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Branch-safe repository for Academic Year records.
 *
 * <p>Academic Year codes, current-year selection and active-year lists are
 * scoped to a branch. No caller should resolve an Academic Year globally by
 * code after Academic Years become branch-owned.</p>
 */
@Repository
public interface AcademicYearRepository
        extends JpaRepository<ErpAcademicYear, Long> {

    List<ErpAcademicYear>
    findAllByBranchBranchIdOrderByStartDateDesc(
            Integer branchId
    );

    List<ErpAcademicYear>
    findAllByBranchBranchIdAndActiveTrueOrderByStartDateDesc(
            Integer branchId
    );

    Optional<ErpAcademicYear>
    findByAcademicYearIdAndBranchBranchId(
            Long academicYearId,
            Integer branchId
    );

    Optional<ErpAcademicYear>
    findByAcademicYearIdAndBranchBranchIdAndActiveTrue(
            Long academicYearId,
            Integer branchId
    );

    Optional<ErpAcademicYear>
    findByBranchBranchIdAndAcademicYearCodeIgnoreCase(
            Integer branchId,
            String academicYearCode
    );

    Optional<ErpAcademicYear>
    findByBranchBranchIdAndAcademicYearCodeIgnoreCaseAndActiveTrue(
            Integer branchId,
            String academicYearCode
    );

    Optional<ErpAcademicYear>
    findByBranchBranchIdAndCurrentYearTrueAndActiveTrue(
            Integer branchId
    );

    boolean existsByBranchBranchIdAndAcademicYearCodeIgnoreCase(
            Integer branchId,
            String academicYearCode
    );

    boolean existsByBranchBranchIdAndAcademicYearCodeIgnoreCaseAndAcademicYearIdNot(
            Integer branchId,
            String academicYearCode,
            Long academicYearId
    );

    boolean existsByBranchBranchIdAndCurrentYearTrueAndActiveTrue(
            Integer branchId
    );

    boolean existsByBranchBranchIdAndCurrentYearTrueAndActiveTrueAndAcademicYearIdNot(
            Integer branchId,
            Long academicYearId
    );

    /**
     * Locks the branch's current Academic Year row while changing the
     * current-year flag, preventing two concurrent requests from creating
     * multiple current years.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT academicYear
            FROM ErpAcademicYear academicYear
            WHERE academicYear.branch.branchId = :branchId
              AND academicYear.currentYear = true
              AND academicYear.active = true
            """)
    Optional<ErpAcademicYear>
    findCurrentYearForUpdate(
            @Param("branchId") Integer branchId
    );

    /**
     * Deactivates the current-year flag for all other Academic Years in the
     * same branch before one Academic Year is made current.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ErpAcademicYear academicYear
            SET academicYear.currentYear = false,
                academicYear.updatedBy = :updatedBy,
                academicYear.updatedAt = CURRENT_TIMESTAMP
            WHERE academicYear.branch.branchId = :branchId
              AND academicYear.academicYearId <> :excludedAcademicYearId
              AND academicYear.currentYear = true
            """)
    int clearCurrentYearForOtherRecords(
            @Param("branchId") Integer branchId,
            @Param("excludedAcademicYearId") Long excludedAcademicYearId,
            @Param("updatedBy") Long updatedBy
    );

    /**
     * Detects date overlap within the same branch.
     */
    @Query("""
            SELECT CASE
                       WHEN COUNT(academicYear) > 0
                       THEN true
                       ELSE false
                   END
            FROM ErpAcademicYear academicYear
            WHERE academicYear.branch.branchId = :branchId
              AND academicYear.active = true
              AND academicYear.startDate <= :endDate
              AND academicYear.endDate >= :startDate
            """)
    boolean existsActiveDateOverlap(
            @Param("branchId") Integer branchId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Detects date overlap while excluding the Academic Year being edited.
     */
    @Query("""
            SELECT CASE
                       WHEN COUNT(academicYear) > 0
                       THEN true
                       ELSE false
                   END
            FROM ErpAcademicYear academicYear
            WHERE academicYear.branch.branchId = :branchId
              AND academicYear.academicYearId <> :academicYearId
              AND academicYear.active = true
              AND academicYear.startDate <= :endDate
              AND academicYear.endDate >= :startDate
            """)
    boolean existsActiveDateOverlapExcludingId(
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}