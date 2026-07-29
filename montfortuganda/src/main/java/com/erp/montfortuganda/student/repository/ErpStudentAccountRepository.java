package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentAccount;
import com.erp.montfortuganda.student.entity.ErpStudentAccount.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpStudentAccountRepository
        extends JpaRepository<ErpStudentAccount, Long>,
        JpaSpecificationExecutor<ErpStudentAccount> {

    /**
     * Loads one Student account while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAccount> findByAccountIdAndBranch_BranchId(
            Long accountId,
            Integer branchId
    );

    /**
     * Finds the account belonging to a Student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAccount>
    findByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds an active account belonging to a Student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAccount>
    findByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds an account using the globally unique username.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAccount> findByUsernameIgnoreCase(
            String username
    );

    /**
     * Finds an account by username while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAccount>
    findByUsernameIgnoreCaseAndBranch_BranchId(
            String username,
            Integer branchId
    );

    /**
     * Finds an account using admission number and branch.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAccount>
    findByAdmissionNoIgnoreCaseAndBranch_BranchId(
            String admissionNo,
            Integer branchId
    );

    /**
     * Prevents multiple accounts for one Student.
     */
    boolean existsByStudent_StudentId(
            Long studentId
    );

    /**
     * Branch-safe Student-account existence check.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Checks whether a username already exists.
     */
    boolean existsByUsernameIgnoreCase(
            String username
    );

    /**
     * Username duplicate check during an update.
     */
    boolean existsByUsernameIgnoreCaseAndAccountIdNot(
            String username,
            Long accountId
    );

    /**
     * Checks whether an admission number already has an account.
     */
    boolean existsByAdmissionNoIgnoreCaseAndBranch_BranchId(
            String admissionNo,
            Integer branchId
    );

    /**
     * Lists all Student accounts belonging to a branch.
     */
    Page<ErpStudentAccount> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active Student accounts belonging to a branch.
     */
    Page<ErpStudentAccount>
    findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists accounts by account status.
     */
    Page<ErpStudentAccount>
    findByBranch_BranchIdAndAccountStatusAndActiveTrue(
            Integer branchId,
            AccountStatus accountStatus,
            Pageable pageable
    );

    /**
     * Lists locked accounts.
     */
    Page<ErpStudentAccount>
    findByBranch_BranchIdAndAccountLockedTrueAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists accounts requiring a password reset.
     */
    Page<ErpStudentAccount>
    findByBranch_BranchIdAndPasswordResetRequiredTrueAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists accounts that have not completed their first password change.
     */
    Page<ErpStudentAccount>
    findByBranch_BranchIdAndPasswordChangedFalseAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    long countByBranch_BranchId(
            Integer branchId
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndAccountStatusAndActiveTrue(
            Integer branchId,
            AccountStatus accountStatus
    );

    long countByBranch_BranchIdAndAccountLockedTrueAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndPasswordResetRequiredTrueAndActiveTrue(
            Integer branchId
    );
}