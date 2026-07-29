package com.erp.montfortuganda.student.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Complete request used to register a new Student.
 *
 * Personal, parent and enrollment information are mandatory.
 * Medical, previous academic history, hostel and transport
 * information are optional.
 *
 * Student identifiers, admission number, Student code, branch,
 * statuses, audit fields and versions are controlled by the backend.
 */
public record StudentCreateRequest(

        /**
         * Optional approved admission application from which the
         * Student is being created.
         */
        @Positive(
                message = "Application ID must be greater than zero."
        )
        Long applicationId,

        @NotNull(
                message = "Student personal information is required."
        )
        @Valid
        StudentPersonalRequest personal,

        @NotNull(
                message = "Student parent information is required."
        )
        @Valid
        StudentParentRequest parent,

        @NotNull(
                message = "Student enrollment information is required."
        )
        @Valid
        StudentEnrollmentRequest enrollment,

        /**
         * Optional medical information.
         */
        @Valid
        StudentMedicalRequest medical,

        /**
         * Optional former-school and examination information.
         */
        @Valid
        StudentAcademicHistoryRequest academicHistory,

        /**
         * Optional hostel allocation.
         *
         * When the Student does not require hostel accommodation,
         * the frontend must send this field as null or omit it.
         */
        @Valid
        StudentHostelRequest hostel,

        /**
         * Optional school transport allocation.
         *
         * When the Student does not require transport, the frontend
         * must send this field as null or omit it.
         */
        @Valid
        StudentTransportRequest transport,

        /**
         * Unique client-generated operation identifier used to prevent
         * accidental duplicate Student submissions.
         */
        @NotBlank(
                message = "Operation ID is required."
        )
        @Size(
                max = 100,
                message = "Operation ID cannot exceed 100 characters."
        )
        String operationId

) {
}