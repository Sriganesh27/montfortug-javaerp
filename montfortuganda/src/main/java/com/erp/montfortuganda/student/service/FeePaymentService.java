package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.fees.entity.ErpStudentFeeAssignment;
import com.erp.montfortuganda.infrastructure.sequence.student.service.StudentSequenceService;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.student.dto.request.FeePaymentCreateRequest;
import com.erp.montfortuganda.student.dto.response.FeePaymentCreateResponse;
import com.erp.montfortuganda.student.entity.ErpStudentFeeLedger;
import com.erp.montfortuganda.student.entity.ErpStudentFeePayment;
import com.erp.montfortuganda.student.repository.ErpStudentFeeAssignmentRepository;
import com.erp.montfortuganda.student.repository.ErpStudentFeeLedgerRepository;
import com.erp.montfortuganda.student.repository.ErpStudentFeePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FeePaymentService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final ErpStudentFeeAssignmentRepository assignmentRepository;
    private final ErpStudentFeePaymentRepository paymentRepository;
    private final ErpStudentFeeLedgerRepository ledgerRepository;
    private final StudentSequenceService studentSequenceService;
    private final CurrentUserService currentUserService;
    private final BranchAccessService branchAccessService;
    private final BranchRepository branchRepository;

    /**
     * Records a successful payment, updates the assignment, and writes the
     * matching ledger credit in one transaction.
     *
     * This method intentionally rejects overpayments. Refunds, reversals and
     * advance-credit handling must use separate audited workflows.
     */
    @Transactional
    public FeePaymentCreateResponse recordPayment(
            Long feeAssignmentId,
            FeePaymentCreateRequest request
    ) {
        if (feeAssignmentId == null || feeAssignmentId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid fee assignment ID is required.");
        }
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment details are required.");
        }

        CurrentUserContext context = currentUserService.getCurrentUserContext();
        Integer branchId = branchAccessService.getValidatedBranchId(context);
        Long actorId = context.getUserId() == null ? null : context.getUserId().longValue();

        ErpStudentFeeAssignment assignment = assignmentRepository
                .findForPaymentUpdate(feeAssignmentId, branchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Fee assignment was not found for this branch."));

        if (!Boolean.TRUE.equals(assignment.getActive())
                || assignment.getFeeStatus() == ErpStudentFeeAssignment.FeeStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A cancelled or inactive fee assignment cannot receive a payment.");
        }

        Branch branch = assignment.getBranch();
        if (branch == null || branch.getBranchId() == null || !branchId.equals(branch.getBranchId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The fee assignment does not belong to the current branch.");
        }
        if (assignment.getStudent() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The fee assignment has no linked student.");
        }

        if (request.amount() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount is required.");
        }
        if (request.paymentMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment mode is required.");
        }
        if (request.amount().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment amount cannot have more than two decimal places.");
        }
        BigDecimal amount = request.amount().setScale(2, RoundingMode.UNNECESSARY);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment amount must be greater than zero.");
        }

        BigDecimal payable = money(assignment.getPayableAmount());
        BigDecimal paidBefore = money(assignment.getPaidAmount());
        BigDecimal balanceBefore = payable.subtract(paidBefore);
        if (balanceBefore.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The assignment has an inconsistent negative balance. Reconcile it before accepting payment.");
        }
        if (balanceBefore.compareTo(BigDecimal.ZERO) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This fee assignment is already fully paid.");
        }
        if (amount.compareTo(balanceBefore) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment exceeds the outstanding balance of " + balanceBefore.toPlainString() + " UGX.");
        }

        // Serialize receipt sequence allocation per branch. This also protects the
        // first payment for a category when its sequence row does not exist yet.
        Branch lockedBranch = branchRepository.findByIdForPaymentSequenceUpdate(branchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The current branch could not be found."));
        if (lockedBranch.getSchoolCode() == null || lockedBranch.getSchoolCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The branch does not have a school code configured; no payment was recorded.");
        }
        branch = lockedBranch;

        String schoolCode = branch.getSchoolCode();
        String categoryCode = normalizeCategory(assignment.getFeeType());
        // Receipt format intentionally has no year segment, so keep one
        // monotonic sequence per branch/category instead of resetting yearly.
        int runningYear = 0;
        String moduleCode = "FEE_PAYMENT_" + categoryCode;
        String actor = actorId == null ? context.getUsername() : actorId.toString();
        long sequence = studentSequenceService.nextSequence(
                branchId, moduleCode, runningYear, actor, 1L);
        String receiptNo = schoolCode.trim().toUpperCase(Locale.ROOT)
                + "-" + categoryCode + "-" + String.format(Locale.ROOT, "%05d", sequence);

        if (paymentRepository.existsByReceiptNoIgnoreCase(receiptNo)) {
            // A sequence collision is treated as a hard error; never silently issue a duplicate receipt.
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The generated receipt number already exists. No payment was recorded; contact support.");
        }

        LocalDateTime paidAt = LocalDateTime.now();
        ErpStudentFeePayment payment = new ErpStudentFeePayment();
        payment.setFeeAssignment(assignment);
        payment.setStudent(assignment.getStudent());
        payment.setBranch(branch);
        payment.setAdmissionNo(assignment.getAdmissionNo());
        payment.setReceiptNo(receiptNo);
        payment.setPaymentDateTime(paidAt);
        payment.setPaymentAmount(amount);
        payment.setExcessAmount(ZERO);
        payment.setPaymentMode(ErpStudentFeePayment.PaymentMode.valueOf(request.paymentMode().name()));
        payment.setTransactionReference(trimToNull(request.transactionReference()));
        payment.setCollectionPoint(trimToNull(request.collectionPoint()));
        payment.setPaymentStatus(ErpStudentFeePayment.PaymentStatus.SUCCESS);
        payment.setReceiptPrinted(false);
        payment.setCollectedBy(actorId);
        payment.setActive(true);
        payment.setRemarks(trimToNull(request.remarks()));
        payment.setCreatedBy(actorId);
        payment.setUpdatedBy(actorId);

        // Flush here so the generated receipt ID is available for the ledger row.
        ErpStudentFeePayment savedPayment = paymentRepository.saveAndFlush(payment);

        BigDecimal paidAfter = paidBefore.add(amount).setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal balanceAfter = payable.subtract(paidAfter).setScale(2, RoundingMode.UNNECESSARY);
        assignment.setPaidAmount(paidAfter);
        assignment.setBalanceAmount(balanceAfter);
        assignment.setUpdatedBy(actorId);

        // The assignment entity recalculates its status and amounts in @PreUpdate.
        // Flush now so a database/optimistic-lock error rolls back the payment and receipt too.
        ErpStudentFeeAssignment savedAssignment = assignmentRepository.saveAndFlush(assignment);
        BigDecimal persistedBalance = money(savedAssignment.getPayableAmount())
                .subtract(money(savedAssignment.getPaidAmount()))
                .max(BigDecimal.ZERO);

        ErpStudentFeeLedger ledger = new ErpStudentFeeLedger();
        ledger.setFeeAssignment(savedAssignment);
        ledger.setFeeReceipt(savedPayment);
        ledger.setStudent(savedAssignment.getStudent());
        ledger.setBranch(branch);
        ledger.setAdmissionNo(savedAssignment.getAdmissionNo());
        ledger.setAcademicYear(savedAssignment.getAcademicYear());
        ledger.setTerm(savedAssignment.getTerm());
        ledger.setFeeName(savedAssignment.getFeeName());
        ledger.setFeeType(savedAssignment.getFeeType());
        ledger.setTransactionType(persistedBalance.compareTo(BigDecimal.ZERO) == 0
                ? ErpStudentFeeLedger.TransactionType.PAYMENT
                : ErpStudentFeeLedger.TransactionType.PARTIAL_PAYMENT);
        ledger.setPaymentMode(ErpStudentFeeLedger.PaymentMode.valueOf(request.paymentMode().name()));
        ledger.setTransactionReference(trimToNull(request.transactionReference()));
        ledger.setTransactionDateTime(paidAt);
        ledger.setDebitAmount(ZERO);
        ledger.setCreditAmount(amount);
        ledger.setRunningBalance(persistedBalance);
        ledger.setCurrency("UGX");
        ledger.setLedgerStatus(ErpStudentFeeLedger.LedgerStatus.ACTIVE);
        ledger.setRemarks("Payment receipt " + receiptNo
                + (trimToNull(request.remarks()) == null ? "" : ". " + request.remarks().trim()));
        ledger.setCreatedBy(actorId);
        ledger.setUpdatedBy(actorId);

        ledgerRepository.saveAndFlush(ledger);

        return new FeePaymentCreateResponse(
                savedPayment.getFeeReceiptId(),
                savedPayment.getReceiptNo(),
                savedPayment.getPaymentDateTime(),
                savedPayment.getPaymentAmount(),
                savedAssignment.getPayableAmount(),
                savedAssignment.getPaidAmount(),
                persistedBalance,
                savedAssignment.getFeeStatus().name(),
                savedPayment.getPaymentStatus().name()
        );
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Uses the fee type already stored on the assignment as the receipt category.
     * Keep this mapping aligned with the school's configured category codes.
     */
    private static String normalizeCategory(String feeType) {
        if (feeType == null || feeType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The fee assignment has no fee type; no receipt can be generated.");
        }
        String code = feeType.trim().toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The fee type cannot be used as a receipt category.");
        }
        return code;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
