package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentFeeAssignment;
import com.erp.montfortuganda.student.entity.ErpStudentFeeAssignment.FeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErpStudentFeeAssignmentRepository
        extends JpaRepository<ErpStudentFeeAssignment, Long>,
        JpaSpecificationExecutor<ErpStudentFeeAssignment> {

    /**
     * Loads one fee assignment while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentFeeAssignment>
    findByFeeAssignmentIdAndBranch_BranchId(
            Long feeAssignmentId,
            Integer branchId
    );

    /**
     * Loads one fee assignment while verifying student and branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentFeeAssignment>
    findByFeeAssignmentIdAndStudent_StudentIdAndBranch_BranchId(
            Long feeAssignmentId,
            Long studentId,
            Integer branchId
    );

    /**
     * Lists all fee assignments belonging to one student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    List<ErpStudentFeeAssignment>
    findByStudent_StudentIdAndBranch_BranchIdOrderByAssignmentDateDescCreatedAtDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Lists active fee assignments belonging to one student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    List<ErpStudentFeeAssignment>
    findByStudent_StudentIdAndBranch_BranchIdAndActiveTrueOrderByAssignmentDateDescCreatedAtDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Lists active fee assignments for a student, academic year and term.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    List<ErpStudentFeeAssignment>
    findByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCaseAndTermIgnoreCaseAndActiveTrueOrderByDueDateAsc(
            Long studentId,
            Integer branchId,
            String academicYear,
            String term
    );

    /**
     * Finds a fee assignment using its unique business combination.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentFeeAssignment>
    findByStudent_StudentIdAndAcademicYearIgnoreCaseAndTermIgnoreCaseAndFeeNameIgnoreCase(
            Long studentId,
            String academicYear,
            String term,
            String feeName
    );

    /**
     * Checks the database unique combination before inserting.
     */
    boolean existsByStudent_StudentIdAndAcademicYearIgnoreCaseAndTermIgnoreCaseAndFeeNameIgnoreCase(
            Long studentId,
            String academicYear,
            String term,
            String feeName
    );

    /**
     * Duplicate check while updating an existing fee assignment.
     */
    boolean existsByStudent_StudentIdAndAcademicYearIgnoreCaseAndTermIgnoreCaseAndFeeNameIgnoreCaseAndFeeAssignmentIdNot(
            Long studentId,
            String academicYear,
            String term,
            String feeName,
            Long feeAssignmentId
    );

    /**
     * Lists all fee assignments belonging to a branch.
     */
    Page<ErpStudentFeeAssignment> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active fee assignments belonging to a branch.
     */
    Page<ErpStudentFeeAssignment> findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active fee assignments for an academic year.
     */
    Page<ErpStudentFeeAssignment>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndActiveTrue(
            Integer branchId,
            String academicYear,
            Pageable pageable
    );

    /**
     * Lists active fee assignments for an academic year and term.
     */
    Page<ErpStudentFeeAssignment>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndTermIgnoreCaseAndActiveTrue(
            Integer branchId,
            String academicYear,
            String term,
            Pageable pageable
    );

    /**
     * Lists fee assignments by status.
     */
    Page<ErpStudentFeeAssignment>
    findByBranch_BranchIdAndFeeStatusAndActiveTrue(
            Integer branchId,
            FeeStatus feeStatus,
            Pageable pageable
    );

    /**
     * Lists fee assignments by fee type.
     */
    Page<ErpStudentFeeAssignment>
    findByBranch_BranchIdAndFeeTypeIgnoreCaseAndActiveTrue(
            Integer branchId,
            String feeType,
            Pageable pageable
    );

    /**
     * Lists fee assignments whose due date is within a range.
     */
    Page<ErpStudentFeeAssignment>
    findByBranch_BranchIdAndDueDateBetweenAndActiveTrue(
            Integer branchId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    /**
     * Finds active unpaid assignments whose due date has passed.
     */
    List<ErpStudentFeeAssignment>
    findByBranch_BranchIdAndDueDateBeforeAndFeeStatusInAndActiveTrue(
            Integer branchId,
            LocalDate dueDate,
            List<FeeStatus> feeStatuses
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndAcademicYearIgnoreCaseAndTermIgnoreCaseAndActiveTrue(
            Integer branchId,
            String academicYear,
            String term
    );

    long countByBranch_BranchIdAndFeeStatusAndActiveTrue(
            Integer branchId,
            FeeStatus feeStatus
    );
}