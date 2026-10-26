package com.erp.montfortuganda.student.dto.response;

import com.erp.montfortuganda.fees.entity.ErpStudentFeeAssignment.FeeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeePaymentAssignmentResponse(
        Long feeAssignmentId,
        Long studentId,
        String admissionNo,
        String studentName,
        String academicYear,
        String term,
        String feeName,
        String feeType,
        BigDecimal totalFee,
        BigDecimal scholarshipAmount,
        BigDecimal concessionAmount,
        BigDecimal discountAmount,
        BigDecimal fineAmount,
        BigDecimal payableAmount,
        BigDecimal paidAmount,
        BigDecimal balanceAmount,
        LocalDate assignmentDate,
        LocalDate dueDate,
        FeeStatus feeStatus,
        boolean active
) {
}
