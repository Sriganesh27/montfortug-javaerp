package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentFeePayment;
import com.erp.montfortuganda.student.entity.ErpStudentFeePayment.PaymentMode;
import com.erp.montfortuganda.student.entity.ErpStudentFeePayment.PaymentStatus;
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
public interface ErpStudentFeePaymentRepository
        extends JpaRepository<ErpStudentFeePayment, Long>,
        JpaSpecificationExecutor<ErpStudentFeePayment> {

    /**
     * Loads one payment receipt while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "student",
            "branch"
    })
    Optional<ErpStudentFeePayment>
    findByFeeReceiptIdAndBranch_BranchId(
            Long feeReceiptId,
            Integer branchId
    );

    /**
     * Loads one payment receipt while verifying student and branch ownership.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "student",
            "branch"
    })
    Optional<ErpStudentFeePayment>
    findByFeeReceiptIdAndStudent_StudentIdAndBranch_BranchId(
            Long feeReceiptId,
            Long studentId,
            Integer branchId
    );

    /**
     * Finds a payment using its globally unique receipt number.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "student",
            "branch"
    })
    Optional<ErpStudentFeePayment>
    findByReceiptNoIgnoreCase(
            String receiptNo
    );

    /**
     * Finds a receipt number while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "student",
            "branch"
    })
    Optional<ErpStudentFeePayment>
    findByReceiptNoIgnoreCaseAndBranch_BranchId(
            String receiptNo,
            Integer branchId
    );

    /**
     * Lists all payments belonging to one student.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "student",
            "branch"
    })
    List<ErpStudentFeePayment>
    findByStudent_StudentIdAndBranch_BranchIdOrderByPaymentDateTimeDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Lists active payments belonging to one student.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "student",
            "branch"
    })
    List<ErpStudentFeePayment>
    findByStudent_StudentIdAndBranch_BranchIdAndActiveTrueOrderByPaymentDateTimeDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Lists payments belonging to one fee assignment.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "student",
            "branch"
    })
    List<ErpStudentFeePayment>
    findByFeeAssignment_FeeAssignmentIdAndBranch_BranchIdOrderByPaymentDateTimeDesc(
            Long feeAssignmentId,
            Integer branchId
    );

    /**
     * Lists successful active payments belonging to one fee assignment.
     */
    @EntityGraph(attributePaths = {
            "feeAssignment",
            "student",
            "branch"
    })
    List<ErpStudentFeePayment>
    findByFeeAssignment_FeeAssignmentIdAndBranch_BranchIdAndPaymentStatusAndActiveTrueOrderByPaymentDateTimeDesc(
            Long feeAssignmentId,
            Integer branchId,
            PaymentStatus paymentStatus
    );

    /**
     * Checks whether a receipt number already exists.
     */
    boolean existsByReceiptNoIgnoreCase(
            String receiptNo
    );

    /**
     * Checks whether another payment uses the receipt during update.
     */
    boolean existsByReceiptNoIgnoreCaseAndFeeReceiptIdNot(
            String receiptNo,
            Long feeReceiptId
    );

    /**
     * Lists all payments belonging to a branch.
     */
    Page<ErpStudentFeePayment> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active payments belonging to a branch.
     */
    Page<ErpStudentFeePayment>
    findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists branch payments by payment status.
     */
    Page<ErpStudentFeePayment>
    findByBranch_BranchIdAndPaymentStatusAndActiveTrue(
            Integer branchId,
            PaymentStatus paymentStatus,
            Pageable pageable
    );

    /**
     * Lists branch payments by payment mode.
     */
    Page<ErpStudentFeePayment>
    findByBranch_BranchIdAndPaymentModeAndActiveTrue(
            Integer branchId,
            PaymentMode paymentMode,
            Pageable pageable
    );

    /**
     * Lists payments received within a date-time range.
     */
    Page<ErpStudentFeePayment>
    findByBranch_BranchIdAndPaymentDateTimeBetweenAndActiveTrue(
            Integer branchId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable
    );

    /**
     * Lists payments using a transaction reference.
     */
    List<ErpStudentFeePayment>
    findByBranch_BranchIdAndTransactionReferenceIgnoreCaseAndActiveTrue(
            Integer branchId,
            String transactionReference
    );

    /**
     * Lists unprinted successful receipts.
     */
    List<ErpStudentFeePayment>
    findByBranch_BranchIdAndPaymentStatusAndReceiptPrintedFalseAndActiveTrueOrderByPaymentDateTimeAsc(
            Integer branchId,
            PaymentStatus paymentStatus
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndPaymentStatusAndActiveTrue(
            Integer branchId,
            PaymentStatus paymentStatus
    );

    long countByBranch_BranchIdAndPaymentModeAndActiveTrue(
            Integer branchId,
            PaymentMode paymentMode
    );

    long countByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(
            Long studentId,
            Integer branchId
    );
}