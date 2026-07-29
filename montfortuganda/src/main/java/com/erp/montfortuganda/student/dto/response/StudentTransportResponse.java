package com.erp.montfortuganda.student.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Student transport-allocation response.
 *
 * Route, vehicle and pickup-point display names are resolved from
 * their respective master tables. Fees, transport status and payment
 * status are controlled by the backend.
 */
public record StudentTransportResponse(

        Long transportAllocationId,

        Long studentId,

        Integer branchId,

        String branchName,

        String admissionNo,

        String academicYear,

        Long routeId,

        String routeName,

        Long vehicleId,

        String vehicleNumber,

        Long pickupPointId,

        String pickupPointName,

        LocalDate transportStartDate,

        LocalDate transportEndDate,

        String seatNumber,

        BigDecimal monthlyFee,

        BigDecimal annualFee,

        BigDecimal discountAmount,

        BigDecimal payableAmount,

        String transportStatus,

        String paymentStatus,

        String emergencyContact,

        String emergencyMobile,

        String remarks,

        Boolean active,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}