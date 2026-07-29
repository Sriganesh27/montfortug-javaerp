package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.entity.ErpStudentEnrollment.EnrollmentStatus;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment.PromotionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request for controlled academic placement changes such as promotion,
 * retention, class transfer, section transfer, withdrawal or graduation.
 *
 * Student, branch, admission number, approval information, audit fields
 * and active state are controlled by the backend.
 */
@SuppressWarnings("unused")
public record StudentEnrollmentUpdateRequest(

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

        @NotNull(message = "Promotion type is required.")
        PromotionType promotionType,

        @NotNull(message = "Enrollment status is required.")
        EnrollmentStatus enrollmentStatus,

        @NotNull(message = "Effective date is required.")
        @PastOrPresent(
                message = "Effective date cannot be in the future."
        )
        LocalDate effectiveDate,

        LocalDate leavingDate,

        @NotBlank(message = "Enrollment change reason is required.")
        @Size(
                max = 255,
                message = "Enrollment change reason cannot exceed 255 characters."
        )
        String changeReason,

        @Size(
                max = 5000,
                message = "Enrollment remarks cannot exceed 5000 characters."
        )
        String remarks,

        /**
         * Must match erp_student_enrollment.version.
         */
        @NotNull(message = "Enrollment record version is required.")
        Long version,

        /**
         * Prevents duplicate processing caused by retries or double-clicks.
         */
        @NotBlank(message = "Operation ID is required.")
        @Size(
                max = 100,
                message = "Operation ID cannot exceed 100 characters."
        )
        String operationId

) {
}