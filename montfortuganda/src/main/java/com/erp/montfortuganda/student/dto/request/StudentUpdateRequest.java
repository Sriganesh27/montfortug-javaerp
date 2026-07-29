package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Request used to update an existing Student profile.
 *
 * Academic-year, class, section, roll number, branch, student code,
 * admission number, status, audit fields and stored file paths cannot
 * be changed through the normal Student profile edit operation.
 */
@SuppressWarnings("unused")
public record StudentUpdateRequest(

        @NotNull(message = "Student personal information is required.")
        @Valid
        StudentPersonalRequest personal,

        @NotNull(message = "Parent or guardian information is required.")
        @Valid
        StudentParentRequest parent,

        /*
         * Optional medical information.
         *
         * When null, the existing medical record is not changed.
         */
        @Valid
        StudentMedicalRequest medical,

        /*
         * Optional previous academic information.
         *
         * When null, the existing academic-history record is not changed.
         */
        @Valid
        StudentAcademicHistoryRequest academicHistory,

        /*
         * Required for optimistic locking.
         *
         * The value must match erp_students.version.
         */
        @NotNull(message = "Student record version is required.")
        Long version

) {
}