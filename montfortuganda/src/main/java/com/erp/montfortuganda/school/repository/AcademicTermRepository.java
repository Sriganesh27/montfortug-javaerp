package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Academic Terms.
 *
 * <p>Terms inherit branch ownership through their Academic Year:</p>
 *
 * <pre>
 * Academic Term -> Academic Year -> Branch
 * </pre>
 *
 * <p>Branch-aware methods are provided for every lookup that may be exposed
 * through a service or controller.</p>
 */
@Repository
public interface AcademicTermRepository
        extends JpaRepository<ErpAcademicTerm, Long> {

    List<ErpAcademicTerm>
    findAllByAcademicYearAcademicYearIdOrderByDisplayOrderAsc(
            Long academicYearId
    );

    List<ErpAcademicTerm>
    findAllByAcademicYearAcademicYearIdAndActiveTrueOrderByDisplayOrderAsc(
            Long academicYearId
    );

    @Query("""
            SELECT term
            FROM ErpAcademicTerm term
            WHERE term.academicYear.academicYearId = :academicYearId
              AND term.academicYear.branch.branchId = :branchId
            ORDER BY term.displayOrder ASC
            """)
    List<ErpAcademicTerm> findAllByBranchAndAcademicYear(
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId
    );

    @Query("""
            SELECT term
            FROM ErpAcademicTerm term
            WHERE term.academicYear.academicYearId = :academicYearId
              AND term.academicYear.branch.branchId = :branchId
              AND term.active = true
            ORDER BY term.displayOrder ASC
            """)
    List<ErpAcademicTerm> findAllActiveByBranchAndAcademicYear(
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId
    );

    @Query("""
            SELECT term
            FROM ErpAcademicTerm term
            WHERE term.termId = :termId
              AND term.academicYear.branch.branchId = :branchId
            """)
    Optional<ErpAcademicTerm> findByTermIdAndBranchId(
            @Param("termId") Long termId,
            @Param("branchId") Integer branchId
    );

    @Query("""
            SELECT term
            FROM ErpAcademicTerm term
            WHERE term.termId = :termId
              AND term.academicYear.branch.branchId = :branchId
              AND term.active = true
            """)
    Optional<ErpAcademicTerm> findActiveByTermIdAndBranchId(
            @Param("termId") Long termId,
            @Param("branchId") Integer branchId
    );

    Optional<ErpAcademicTerm>
    findByAcademicYearAcademicYearIdAndTermCodeIgnoreCase(
            Long academicYearId,
            String termCode
    );

    Optional<ErpAcademicTerm>
    findByAcademicYearAcademicYearIdAndTermCodeIgnoreCaseAndActiveTrue(
            Long academicYearId,
            String termCode
    );

    Optional<ErpAcademicTerm>
    findByAcademicYearAcademicYearIdAndCurrentTermTrueAndActiveTrue(
            Long academicYearId
    );

    @Query("""
            SELECT term
            FROM ErpAcademicTerm term
            WHERE term.academicYear.branch.branchId = :branchId
              AND term.currentTerm = true
              AND term.active = true
              AND term.academicYear.currentYear = true
              AND term.academicYear.active = true
            """)
    Optional<ErpAcademicTerm> findCurrentTermByBranchId(
            @Param("branchId") Integer branchId
    );

    boolean existsByAcademicYearAcademicYearIdAndTermCodeIgnoreCase(
            Long academicYearId,
            String termCode
    );

    boolean existsByAcademicYearAcademicYearIdAndTermCodeIgnoreCaseAndTermIdNot(
            Long academicYearId,
            String termCode,
            Long termId
    );

    boolean existsByAcademicYearAcademicYearIdAndDisplayOrder(
            Long academicYearId,
            Integer displayOrder
    );

    boolean existsByAcademicYearAcademicYearIdAndDisplayOrderAndTermIdNot(
            Long academicYearId,
            Integer displayOrder,
            Long termId
    );

    boolean existsByAcademicYearAcademicYearIdAndCurrentTermTrueAndActiveTrue(
            Long academicYearId
    );

    boolean existsByAcademicYearAcademicYearIdAndCurrentTermTrueAndActiveTrueAndTermIdNot(
            Long academicYearId,
            Long termId
    );

    /**
     * Locks the current Term of one Academic Year while the current-term flag
     * is being changed.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT term
            FROM ErpAcademicTerm term
            WHERE term.academicYear.academicYearId = :academicYearId
              AND term.currentTerm = true
              AND term.active = true
            """)
    Optional<ErpAcademicTerm> findCurrentTermForUpdate(
            @Param("academicYearId") Long academicYearId
    );

    /**
     * Clears the current flag from all other Terms in the selected Academic
     * Year.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ErpAcademicTerm term
            SET term.currentTerm = false,
                term.updatedBy = :updatedBy,
                term.updatedAt = CURRENT_TIMESTAMP
            WHERE term.academicYear.academicYearId = :academicYearId
              AND term.termId <> :excludedTermId
              AND term.currentTerm = true
            """)
    int clearCurrentTermForOtherRecords(
            @Param("academicYearId") Long academicYearId,
            @Param("excludedTermId") Long excludedTermId,
            @Param("updatedBy") Long updatedBy
    );

    /**
     * Detects active Term date overlap inside one Academic Year.
     */
    @Query("""
            SELECT CASE
                       WHEN COUNT(term) > 0
                       THEN true
                       ELSE false
                   END
            FROM ErpAcademicTerm term
            WHERE term.academicYear.academicYearId = :academicYearId
              AND term.active = true
              AND term.startDate <= :endDate
              AND term.endDate >= :startDate
            """)
    boolean existsActiveDateOverlap(
            @Param("academicYearId") Long academicYearId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Detects active Term date overlap while excluding the Term being edited.
     */
    @Query("""
            SELECT CASE
                       WHEN COUNT(term) > 0
                       THEN true
                       ELSE false
                   END
            FROM ErpAcademicTerm term
            WHERE term.academicYear.academicYearId = :academicYearId
              AND term.termId <> :termId
              AND term.active = true
              AND term.startDate <= :endDate
              AND term.endDate >= :startDate
            """)
    boolean existsActiveDateOverlapExcludingId(
            @Param("academicYearId") Long academicYearId,
            @Param("termId") Long termId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}