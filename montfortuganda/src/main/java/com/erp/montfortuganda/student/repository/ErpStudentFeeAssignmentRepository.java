package com.erp.montfortuganda.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.erp.montfortuganda.fees.entity.ErpStudentFeeAssignment;
import com.erp.montfortuganda.fees.entity.ErpStudentFeeAssignment.FeeStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErpStudentFeeAssignmentRepository
        extends JpaRepository<ErpStudentFeeAssignment, Long>,
        JpaSpecificationExecutor<ErpStudentFeeAssignment> {

    /**
     * Locks a fee assignment for payment processing and enforces branch ownership.
     * This prevents concurrent payments from using the same stale outstanding balance.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select assignment
            from ErpStudentFeeAssignment assignment
            join fetch assignment.student
            join fetch assignment.branch
            where assignment.feeAssignmentId = :feeAssignmentId
              and assignment.branch.branchId = :branchId
            """)
    Optional<ErpStudentFeeAssignment> findForPaymentUpdate(
            @Param("feeAssignmentId") Long feeAssignmentId,
            @Param("branchId") Integer branchId
    );

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
     * Checks for a duplicate assignment in the same academic year and term.
     */
    boolean existsByStudent_StudentIdAndAcademicYearIgnoreCaseAndTermIgnoreCaseAndFeeNameIgnoreCase(
            Long studentId,
            String academicYear,
            String term,
            String feeName
    );

    /**
     * Same-term duplicate check scoped to the student's branch and active records.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCaseAndTermIgnoreCaseAndFeeNameIgnoreCaseAndActiveTrue(
            Long studentId,
            Integer branchId,
            String academicYear,
            String term,
            String feeName
    );

    /**
     * Checks whether a per-academic-year fee already exists in any term of that year.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCaseAndFeeNameIgnoreCaseAndFeeTypeIgnoreCaseAndActiveTrue(
            Long studentId,
            Integer branchId,
            String academicYear,
            String feeName,
            String feeType
    );

    /**
     * Checks whether a one-time or admission fee has already been assigned in this branch.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchIdAndFeeNameIgnoreCaseAndFeeTypeIgnoreCaseAndActiveTrue(
            Long studentId,
            Integer branchId,
            String feeName,
            String feeType
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
