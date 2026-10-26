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

        Boolean scholarshipRequired,

        @PastOrPresent(
                message = "Joining date cannot be in the future."
        )
        LocalDate joiningDate,

        @Size(
                max = 5000,
                message = "Enrollment remarks cannot exceed 5000 characters."
        )
        String remarks,

        /**
         * Day arrangement for this academic-year enrollment.
         * The backend validates whether HALF_DAY is permitted for the class.
         */
        String schoolDayType,

        /**
         * Optional joining term. When converting an admission application,
         * the application's validated joining term takes precedence.
         */
        Long termId
) {

    /**
     * Backward-compatible constructor for existing call sites that provide
     * schoolDayType but do not provide termId.
     */
    public StudentEnrollmentRequest(
            Long academicYearId,
            Integer classId,
            Long sectionId,
            String rollNo,
            AdmissionType admissionType,
            Boolean scholarshipRequired,
            LocalDate joiningDate,
            String remarks,
            String schoolDayType
    ) {
        this(
                academicYearId,
                classId,
                sectionId,
                rollNo,
                admissionType,
                scholarshipRequired,
                joiningDate,
                remarks,
                schoolDayType,
                null
        );
    }

    /**
     * Backward-compatible constructor for existing call sites that do not
     * supply either a day type or a term. Older calls default to FULL_DAY.
     */
    public StudentEnrollmentRequest(
            Long academicYearId,
            Integer classId,
            Long sectionId,
            String rollNo,
            AdmissionType admissionType,
            Boolean scholarshipRequired,
            LocalDate joiningDate,
            String remarks
    ) {
        this(
                academicYearId,
                classId,
                sectionId,
                rollNo,
                admissionType,
                scholarshipRequired,
                joiningDate,
                remarks,
                "FULL_DAY",
                null
        );
    }
}
