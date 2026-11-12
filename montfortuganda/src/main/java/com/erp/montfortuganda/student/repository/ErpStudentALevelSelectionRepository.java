package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentALevelSelection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Branch-aware persistence operations for current and historical selections. */
@Repository
public interface ErpStudentALevelSelectionRepository
        extends JpaRepository<ErpStudentALevelSelection, Long> {

    Collection<ErpStudentALevelSelection.SelectionStatus> CURRENT_STATUSES = List.of(
            ErpStudentALevelSelection.SelectionStatus.PENDING,
            ErpStudentALevelSelection.SelectionStatus.CONFIRMED
    );

    /** Loads selection history for one application. */
    @EntityGraph(attributePaths = {"combination", "combination.principalSubject1",
            "combination.principalSubject2", "combination.principalSubject3",
            "combination.subsidiarySubject"})
    List<ErpStudentALevelSelection> findAllByApplication_ApplicationIdOrderByCreatedAtDesc(
            Long applicationId
    );

    /** Loads selection history for one student, newest academic year first. */
    @EntityGraph(attributePaths = {"combination", "combination.principalSubject1",
            "combination.principalSubject2", "combination.principalSubject3",
            "combination.subsidiarySubject", "academicYear", "schoolClass"})
    List<ErpStudentALevelSelection>
    findAllByStudent_StudentIdAndBranch_BranchIdOrderByAcademicYear_AcademicYearIdDescCreatedAtDesc(
            Long studentId,
            Integer branchId
    );

    /** Finds the current selection for an application and its placement. */
    @EntityGraph(attributePaths = {"combination", "branch", "academicYear", "schoolClass"})
    Optional<ErpStudentALevelSelection>
    findFirstByApplication_ApplicationIdAndAcademicYear_AcademicYearIdAndSchoolClass_ClassIdAndStatusInOrderByCreatedAtDesc(
            Long applicationId,
            Long academicYearId,
            Integer classId,
            Collection<ErpStudentALevelSelection.SelectionStatus> statuses
    );

    /** Finds the current selection for a student and its placement. */
    @EntityGraph(attributePaths = {"combination", "branch", "academicYear", "schoolClass"})
    Optional<ErpStudentALevelSelection>
    findFirstByStudent_StudentIdAndAcademicYear_AcademicYearIdAndSchoolClass_ClassIdAndStatusInOrderByCreatedAtDesc(
            Long studentId,
            Long academicYearId,
            Integer classId,
            Collection<ErpStudentALevelSelection.SelectionStatus> statuses
    );

    /**
     * Locks an application's current selection before confirmation or
     * replacement. The service must also verify branch ownership.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT selection
            FROM ErpStudentALevelSelection selection
            WHERE selection.application.applicationId = :applicationId
              AND selection.branch.branchId = :branchId
              AND selection.academicYear.academicYearId = :academicYearId
              AND selection.schoolClass.classId = :classId
              AND selection.status IN :statuses
            """)
    Optional<ErpStudentALevelSelection> findCurrentApplicationSelectionForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("classId") Integer classId,
            @Param("statuses") Collection<ErpStudentALevelSelection.SelectionStatus> statuses
    );

    /** Locks a student's current selection before confirmation or replacement. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT selection
            FROM ErpStudentALevelSelection selection
            WHERE selection.student.studentId = :studentId
              AND selection.branch.branchId = :branchId
              AND selection.academicYear.academicYearId = :academicYearId
              AND selection.schoolClass.classId = :classId
              AND selection.status IN :statuses
            """)
    Optional<ErpStudentALevelSelection> findCurrentStudentSelectionForUpdate(
            @Param("studentId") Long studentId,
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("classId") Integer classId,
            @Param("statuses") Collection<ErpStudentALevelSelection.SelectionStatus> statuses
    );

    boolean existsByCombination_CombinationIdAndStatusIn(
            Long combinationId,
            Collection<ErpStudentALevelSelection.SelectionStatus> statuses
    );

    long countByCombination_CombinationIdAndStatusIn(
            Long combinationId,
            Collection<ErpStudentALevelSelection.SelectionStatus> statuses
    );
}
