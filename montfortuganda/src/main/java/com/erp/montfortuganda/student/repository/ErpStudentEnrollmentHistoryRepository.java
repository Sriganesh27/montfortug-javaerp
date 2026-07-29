package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentEnrollmentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErpStudentEnrollmentHistoryRepository
        extends JpaRepository<ErpStudentEnrollmentHistory, Long> {

    /**
     * Loads one history record while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "enrollment", "branch"})
    Optional<ErpStudentEnrollmentHistory>
    findByEnrollmentHistoryIdAndBranch_BranchId(
            Long enrollmentHistoryId,
            Integer branchId
    );

    /**
     * Returns the complete enrollment history of one student,
     * with the newest effective record first.
     */
    @EntityGraph(attributePaths = {"student", "enrollment", "branch"})
    List<ErpStudentEnrollmentHistory>
    findByStudent_StudentIdAndBranch_BranchIdOrderByEffectiveDateDescCreatedAtDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Paginated enrollment history for one student.
     */
    @EntityGraph(attributePaths = {"student", "enrollment", "branch"})
    Page<ErpStudentEnrollmentHistory>
    findByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId,
            Pageable pageable
    );

    /**
     * Finds history records created from a specific current enrollment.
     */
    @EntityGraph(attributePaths = {"student", "enrollment", "branch"})
    List<ErpStudentEnrollmentHistory>
    findByEnrollment_EnrollmentIdAndBranch_BranchIdOrderByEffectiveDateDescCreatedAtDesc(
            Long enrollmentId,
            Integer branchId
    );

    /**
     * Lists enrollment-history records for an academic year.
     */
    Page<ErpStudentEnrollmentHistory>
    findByBranch_BranchIdAndAcademicYearId(
            Integer branchId,
            Long academicYearId,
            Pageable pageable
    );

    /**
     * Lists history records for a class and section.
     */
    Page<ErpStudentEnrollmentHistory>
    findByBranch_BranchIdAndAcademicYearIdAndClassIdAndSectionId(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Long sectionId,
            Pageable pageable
    );

    /**
     * Lists history records for a class when no section is selected.
     */
    Page<ErpStudentEnrollmentHistory>
    findByBranch_BranchIdAndAcademicYearIdAndClassId(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Pageable pageable
    );

    /**
     * Lists records effective within a specified date range.
     */
    Page<ErpStudentEnrollmentHistory>
    findByBranch_BranchIdAndEffectiveDateBetween(
            Integer branchId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    /**
     * Lists records by enrollment status.
     */
    Page<ErpStudentEnrollmentHistory>
    findByBranch_BranchIdAndEnrollmentStatusIgnoreCase(
            Integer branchId,
            String enrollmentStatus,
            Pageable pageable
    );

    /**
     * Checks whether a student already has enrollment history.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Counts all history records for a student.
     */
    long countByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Counts history records for an academic year.
     */
    long countByBranch_BranchIdAndAcademicYearId(
            Integer branchId,
            Long academicYearId
    );
}