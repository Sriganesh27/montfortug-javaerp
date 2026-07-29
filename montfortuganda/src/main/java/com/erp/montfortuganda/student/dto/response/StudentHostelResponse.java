package com.erp.montfortuganda.student.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Student hostel-allocation response.
 *
 * Hostel, room and bed names are resolved from their master tables.
 * Fees, allocation status and payment status are controlled by the backend.
 */
public record StudentHostelResponse(

        Long hostelAllocationId,

        Long studentId,

        Integer branchId,

        String branchName,

        String admissionNo,

        String academicYear,

        Long hostelId,

        String hostelName,

        Long roomId,

        String roomName,

        Long bedId,

        String bedName,

        LocalDate allocationStartDate,

        LocalDate allocationEndDate,

        BigDecimal monthlyFee,

        BigDecimal annualFee,

        BigDecimal discountAmount,

        BigDecimal payableAmount,

        String allocationStatus,

        String paymentStatus,

        String localGuardianName,

        String localGuardianMobile,

        String localGuardianRelation,

        String remarks,

        Boolean active,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}