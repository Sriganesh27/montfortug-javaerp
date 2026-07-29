package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpParent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpParentRepository
        extends JpaRepository<ErpParent, Long> {

    /**
     * Loads one parent record while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpParent> findByParentIdAndBranch_BranchId(
            Long parentId,
            Integer branchId
    );

    /**
     * Finds the parent/guardian details belonging to a student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpParent> findByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds active parent/guardian details belonging to a student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpParent> findByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds parent information using the student's admission number.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpParent> findByAdmissionNoIgnoreCaseAndBranch_BranchId(
            String admissionNo,
            Integer branchId
    );

    /**
     * Prevents multiple parent records for the same student.
     */
    boolean existsByStudent_StudentId(
            Long studentId
    );

    /**
     * Branch-safe duplicate check.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Checks for another parent row during update.
     */
    boolean existsByStudent_StudentIdAndParentIdNot(
            Long studentId,
            Long parentId
    );

    /**
     * Lists all parent/guardian records in a branch.
     */
    Page<ErpParent> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists only active parent/guardian records in a branch.
     */
    Page<ErpParent> findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    long countByBranch_BranchId(
            Integer branchId
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );
}