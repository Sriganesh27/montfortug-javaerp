package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentFeeLedger;
import com.erp.montfortuganda.student.entity.ErpStudentFeeLedger.LedgerStatus;
import com.erp.montfortuganda.student.entity.ErpStudentFeeLedger.PaymentMode;
import com.erp.montfortuganda.student.entity.ErpStudentFeeLedger.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErpStudentFeeLedgerRepository
        extends JpaRepository<ErpStudentFeeLedger, Long>,
        JpaSpecificationExecutor<ErpStudentFeeLedger> {

    /**
     * Loads one ledger entry while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "feeReceipt",
            "student",
            "branch"
    })
    Optional<ErpStudentFeeLedger> findByFeeLedgerIdAndBranch_BranchId(
            Long feeLedgerId,
            Integer branchId
    );

    /**
     * Loads one ledger entry while verifying student and branch ownership.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "feeReceipt",
            "student",
            "branch"
    })
    Optional<ErpStudentFeeLedger>
    findByFeeLedgerIdAndStudent_StudentIdAndBranch_BranchId(
            Long feeLedgerId,
            Long studentId,
            Integer branchId
    );

    /**
     * Returns a student's complete ledger in chronological order.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "feeReceipt",
            "student",
            "branch"
    })
    List<ErpStudentFeeLedger>
    findByStudent_StudentIdAndBranch_BranchIdOrderByTransactionDateTimeAscFeeLedgerIdAsc(
            Long studentId,
            Integer branchId
    );

    /**
     * Returns a student's latest ledger entries first.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "feeReceipt",
            "student",
            "branch"
    })
    Page<ErpStudentFeeLedger>
    findByStudent_StudentIdAndBranch_BranchIdOrderByTransactionDateTimeDescFeeLedgerIdDesc(
            Long studentId,
            Integer branchId,
            Pageable pageable
    );

    /**
     * Returns ledger entries for one fee assignment.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "feeReceipt",
            "student",
            "branch"
    })
    List<ErpStudentFeeLedger>
    findByFeeAssignment_FeeAssignmentIdAndBranch_BranchIdOrderByTransactionDateTimeAscFeeLedgerIdAsc(
            Long feeAssignmentId,
            Integer branchId
    );

    /**
     * Returns ledger entries connected to one payment receipt.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "feeReceipt",
            "student",
            "branch"
    })
    List<ErpStudentFeeLedger>
    findByFeeReceipt_FeeReceiptIdAndBranch_BranchIdOrderByTransactionDateTimeAscFeeLedgerIdAsc(
            Long feeReceiptId,
            Integer branchId
    );

    /**
     * Returns the latest ledger event for a fee assignment.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "feeReceipt",
            "student",
            "branch"
    })
    Optional<ErpStudentFeeLedger>
    findFirstByFeeAssignment_FeeAssignmentIdAndBranch_BranchIdOrderByTransactionDateTimeDescFeeLedgerIdDesc(
            Long feeAssignmentId,
            Integer branchId
    );

    /**
     * Returns the latest ledger event for a student.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "feeReceipt",
            "student",
            "branch"
    })
    Optional<ErpStudentFeeLedger>
    findFirstByStudent_StudentIdAndBranch_BranchIdOrderByTransactionDateTimeDescFeeLedgerIdDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds entries using an external transaction reference.
     */
    List<ErpStudentFeeLedger>
    findByBranch_BranchIdAndTransactionReferenceIgnoreCaseOrderByTransactionDateTimeDesc(
            Integer branchId,
            String transactionReference
    );

    /**
     * Lists all ledger entries belonging to a branch.
     */
    Page<ErpStudentFeeLedger> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists ledger entries by status.
     */
    Page<ErpStudentFeeLedger>
    findByBranch_BranchIdAndLedgerStatus(
            Integer branchId,
            LedgerStatus ledgerStatus,
            Pageable pageable
    );

    /**
     * Lists ledger entries by transaction type.
     */
    Page<ErpStudentFeeLedger>
    findByBranch_BranchIdAndTransactionType(
            Integer branchId,
            TransactionType transactionType,
            Pageable pageable
    );

    /**
     * Lists ledger entries by payment mode.
     */
    Page<ErpStudentFeeLedger>
    findByBranch_BranchIdAndPaymentMode(
            Integer branchId,
            PaymentMode paymentMode,
            Pageable pageable
    );

    /**
     * Lists ledger entries for an academic year and term.
     */
    Page<ErpStudentFeeLedger>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndTermIgnoreCase(
            Integer branchId,
            String academicYear,
            String term,
            Pageable pageable
    );

    /**
     * Lists ledger entries for a fee name.
     */
    Page<ErpStudentFeeLedger>
    findByBranch_BranchIdAndFeeNameIgnoreCase(
            Integer branchId,
            String feeName,
            Pageable pageable
    );

    /**
     * Lists transactions within a date-time range.
     */
    Page<ErpStudentFeeLedger>
    findByBranch_BranchIdAndTransactionDateTimeBetween(
            Integer branchId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable
    );

    /**
     * Checks whether a payment receipt already has a ledger entry
     * of the specified transaction type.
     */
    boolean existsByFeeReceipt_FeeReceiptIdAndTransactionType(
            Long feeReceiptId,
            TransactionType transactionType
    );

    /**
     * Checks whether an assignment contains a ledger event of a type.
     */
    boolean existsByFeeAssignment_FeeAssignmentIdAndTransactionType(
            Long feeAssignmentId,
            TransactionType transactionType
    );

    long countByBranch_BranchId(
            Integer branchId
    );

    long countByBranch_BranchIdAndLedgerStatus(
            Integer branchId,
            LedgerStatus ledgerStatus
    );

    long countByBranch_BranchIdAndTransactionType(
            Integer branchId,
            TransactionType transactionType
    );

    long countByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );
}