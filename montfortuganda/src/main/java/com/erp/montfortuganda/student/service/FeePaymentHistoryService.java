package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.student.dto.response.FeePaymentHistoryResponse;
import com.erp.montfortuganda.student.entity.ErpStudentFeePayment;
import com.erp.montfortuganda.student.repository.ErpStudentFeePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeePaymentHistoryService {

    private final ErpStudentFeePaymentRepository paymentRepository;
    private final CurrentUserService currentUserService;
    private final BranchAccessService branchAccessService;

    /**
     * Returns active payment records for one student in the authenticated
     * user's branch, newest first.
     */
    public List<FeePaymentHistoryResponse> getStudentPaymentHistory(
            Long studentId
    ) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be greater than zero."
            );
        }

        Integer branchId = branchAccessService.getValidatedBranchId(
                currentUserService.getCurrentUserContext()
        );

        return paymentRepository
                .findByStudent_StudentIdAndBranch_BranchIdAndActiveTrueOrderByPaymentDateTimeDesc(
                        studentId,
                        branchId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FeePaymentHistoryResponse toResponse(
            ErpStudentFeePayment payment
    ) {
        var assignment = payment.getFeeAssignment();
        var student = payment.getStudent();

        return new FeePaymentHistoryResponse(
                payment.getFeeReceiptId(),
                payment.getReceiptNo(),
                student.getStudentId(),
                student.getAdmissionNo(),
                student.getFullName(),
                assignment.getFeeAssignmentId(),
                assignment.getFeeName(),
                assignment.getFeeType(),
                assignment.getAcademicYear(),
                assignment.getTerm(),
                payment.getPaymentDateTime(),
                payment.getPaymentAmount(),
                payment.getExcessAmount(),
                payment.getPaymentMode(),
                payment.getTransactionReference(),
                payment.getCollectionPoint(),
                payment.getPaymentStatus(),
                Boolean.TRUE.equals(payment.getReceiptPrinted()),
                payment.getRemarks()
        );
    }
}
