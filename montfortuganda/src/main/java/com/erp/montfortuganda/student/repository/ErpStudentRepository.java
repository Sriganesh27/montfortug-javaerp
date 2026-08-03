package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Branch-safe persistence operations for the Student master table.
 * Browser-supplied student IDs must always be resolved together with the
 * authenticated user's branch ID.
 */
@Repository
public interface ErpStudentRepository
        extends JpaRepository<ErpStudent, Long>,
        JpaSpecificationExecutor<ErpStudent> {

    /**
     * Loads the Student profile and its main one-to-one relationships while
     * enforcing branch ownership.
     */
    @EntityGraph(
            attributePaths = {
                    "branch",
                    "application",
                    "currentEnrollment",
                    "academicHistory"
            }
    )
    Optional<ErpStudent> findByStudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Loads an active Student while enforcing branch ownership.
     */
    @EntityGraph(
            attributePaths = {
                    "branch",
                    "application",
                    "currentEnrollment"
            }
    )
    Optional<ErpStudent> findByStudentIdAndBranch_BranchIdAndActiveTrue(
            Long studentId,
            Integer branchId
    );

    /**
     * Branch-safe admission-number lookup.
     */
    Optional<ErpStudent> findByAdmissionNoIgnoreCaseAndBranch_BranchId(
            String admissionNo,
            Integer branchId
    );

    /**
     * Finds a Student created from an admission application.
     */
    Optional<ErpStudent> findByApplication_ApplicationIdAndBranch_BranchId(
            Long applicationId,
            Integer branchId
    );

    /**
     * Global duplicate check matching the current database unique constraint
     * on admission_no.
     */
    boolean existsByAdmissionNoIgnoreCase(
            String admissionNo
    );

    /**
     * Global duplicate check used during Student updates.
     */
    boolean existsByAdmissionNoIgnoreCaseAndStudentIdNot(
            String admissionNo,
            Long studentId
    );

    /**
     * Prevents one admission application from creating multiple Students.
     */
    boolean existsByApplication_ApplicationId(
            Long applicationId
    );

    /**
     * Paginated branch Student list.
     */
    Page<ErpStudent> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Paginated list containing only active Students in a branch.
     */
    Page<ErpStudent> findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Counts all Students belonging to a branch.
     */
    long countByBranch_BranchId(
            Integer branchId
    );

    /**
     * Counts active Students belonging to a branch.
     */
    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    /**
     * Counts Students by status within a branch.
     */
    long countByBranch_BranchIdAndStudentStatusIgnoreCase(
            Integer branchId,
            String studentStatus
    );
}