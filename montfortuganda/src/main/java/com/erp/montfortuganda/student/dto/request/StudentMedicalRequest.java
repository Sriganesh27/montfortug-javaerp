package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.entity.ErpStudentMedical.BloodGroup;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Optional medical and health information for a Student.
 *
 * Medical ID, Student ID, branch, admission number, active status,
 * audit fields and version are controlled by the backend.
 */
@SuppressWarnings("unused")
public record StudentMedicalRequest(

        BloodGroup bloodGroup,

        @DecimalMin(
                value = "0.01",
                message = "Height must be greater than zero."
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "Height can contain up to 3 whole-number digits and 2 decimal places."
        )
        BigDecimal heightCm,

        @DecimalMin(
                value = "0.01",
                message = "Weight must be greater than zero."
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "Weight can contain up to 3 whole-number digits and 2 decimal places."
        )
        BigDecimal weightKg,

        @Size(
                max = 500,
                message = "Allergies cannot exceed 500 characters."
        )
        String allergies,

        @Size(
                max = 500,
                message = "Chronic conditions cannot exceed 500 characters."
        )
        String chronicConditions,

        @Size(
                max = 500,
                message = "Ongoing medication cannot exceed 500 characters."
        )
        String ongoingMedication,

        @Size(
                max = 500,
                message = "Special needs cannot exceed 500 characters."
        )
        String specialNeeds,

        Boolean fitForSports,

        @Size(
                max = 150,
                message = "Emergency doctor name cannot exceed 150 characters."
        )
        String emergencyDoctorName,

        @Pattern(
                regexp = "^[0-9+\\- ]{7,20}$",
                message = "Emergency doctor mobile number is invalid."
        )
        @Size(
                max = 20,
                message = "Emergency doctor mobile number cannot exceed 20 characters."
        )
        String emergencyDoctorMobile,

        @Size(
                max = 150,
                message = "Preferred hospital cannot exceed 150 characters."
        )
        String preferredHospital,

        @Size(
                max = 500,
                message = "Medical remarks cannot exceed 500 characters."
        )
        String remarks
) {
}