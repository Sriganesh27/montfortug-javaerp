package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpStudentEnrollmentRepository
        extends JpaRepository<ErpStudentEnrollment, Long> {

    /**
     * Loads an enrollment by ID while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentEnrollment> findByEnrollmentIdAndBranch_BranchId(
            Long enrollmentId,
            Integer branchId
    );

    /**
     * Finds the student's current enrollment in the authenticated branch.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentEnrollment> findByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds the student's active current enrollment.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentEnrollment>
    findByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(
            Long studentId,
            Integer branchId
    );

    /**
     * Checks whether a current enrollment already exists for a student.
     */
    boolean existsByStudent_StudentId(
            Long studentId
    );

    /**
     * Checks whether a current enrollment exists for a student in a branch.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Detects duplicate roll numbers within the same academic placement.
     */
    boolean existsByBranch_BranchIdAndAcademicYearIdAndClassIdAndSectionIdAndRollNoIgnoreCase(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Long sectionId,
            String rollNo
    );

    /**
     * Duplicate roll-number check during enrollment update.
     */
    boolean existsByBranch_BranchIdAndAcademicYearIdAndClassIdAndSectionIdAndRollNoIgnoreCaseAndEnrollmentIdNot(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Long sectionId,
            String rollNo,
            Long enrollmentId
    );

    /**
     * Lists all current enrollments belonging to a branch.
     */
    Page<ErpStudentEnrollment> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active current enrollments belonging to a branch.
     */
    Page<ErpStudentEnrollment> findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active students for a class and section.
     */
    Page<ErpStudentEnrollment>
    findByBranch_BranchIdAndAcademicYearIdAndClassIdAndSectionIdAndActiveTrue(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Long sectionId,
            Pageable pageable
    );

    /**
     * Lists active students for a class when a section is not selected.
     */
    Page<ErpStudentEnrollment>
    findByBranch_BranchIdAndAcademicYearIdAndClassIdAndActiveTrue(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Pageable pageable
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndAcademicYearIdAndClassIdAndSectionIdAndActiveTrue(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Long sectionId
    );
}