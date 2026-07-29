package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.entity.ErpStudentEnrollment.AdmissionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Initial academic enrollment information submitted during Student creation.
 *
 * Student, branch, admission number, promotion type, enrollment status,
 * approval details, active flag, audit fields and version are controlled
 * by the backend.
 */
@SuppressWarnings("unused")
public record StudentEnrollmentRequest(

        @NotNull(message = "Academic year is required.")
        Long academicYearId,

        @NotNull(message = "Class is required.")
        Integer classId,

        Long sectionId,

        @Size(
                max = 20,
                message = "Roll number cannot exceed 20 characters."
        )
        String rollNo,

        @NotNull(message = "Admission type is required.")
        AdmissionType admissionType,

        @NotNull(message = "Joining date is required.")
        @PastOrPresent(
                message = "Joining date cannot be in the future."
        )
        LocalDate joiningDate,

        @Size(
                max = 5000,
                message = "Enrollment remarks cannot exceed 5000 characters."
        )
        String remarks
) {
}