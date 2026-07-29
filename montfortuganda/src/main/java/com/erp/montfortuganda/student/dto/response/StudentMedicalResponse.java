package com.erp.montfortuganda.student.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Student medical, health and biometric information.
 *
 * This response should only be returned to users who have permission
 * to view sensitive Student medical details.
 */
public record StudentMedicalResponse(

        Long medicalId,

        Long studentId,

        Integer branchId,

        String admissionNo,

        /**
         * Enum value stored by the backend, for example:
         * A_PLUS, O_MINUS or UNKNOWN.
         */
        String bloodGroup,

        /**
         * User-friendly blood-group value, for example:
         * A+, O- or UNKNOWN.
         */
        String bloodGroupCode,

        BigDecimal heightCm,

        BigDecimal weightKg,

        String allergies,

        String chronicConditions,

        String ongoingMedication,

        String specialNeeds,

        Boolean fitForSports,

        String emergencyDoctorName,

        String emergencyDoctorMobile,

        String preferredHospital,

        String remarks,

        Boolean active,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}