package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.enums.StudentGender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Student personal and residential information submitted during
 * Student registration or profile editing.
 *
 * Student ID, branch, admission number, full name, status,
 * file path, active flag, audit fields and entity version
 * are controlled by the backend.
 */
@SuppressWarnings("unused")
public record StudentPersonalRequest(

        @Size(
                max = 50,
                message = "Learner LIN cannot exceed 50 characters."
        )
        String learnerLin,

        @NotNull(message = "Admission year is required.")
        @Min(
                value = 1900,
                message = "Admission year must be 1900 or later."
        )
        @Max(
                value = 2100,
                message = "Admission year cannot exceed 2100."
        )
        Integer admissionYear,

        Integer joiningClassId,

        Long joiningTermId,

        @NotBlank(message = "Student first name is required.")
        @Size(
                max = 100,
                message = "Student first name cannot exceed 100 characters."
        )
        String firstName,

        @Size(
                max = 100,
                message = "Student middle name cannot exceed 100 characters."
        )
        String middleName,

        @Size(
                max = 100,
                message = "Student last name cannot exceed 100 characters."
        )
        String lastName,

        @NotNull(message = "Student gender is required.")
        StudentGender gender,

        @NotNull(message = "Student date of birth is required.")
        @Past(message = "Student date of birth must be in the past.")
        LocalDate dateOfBirth,

        @Size(
                max = 100,
                message = "Student nationality cannot exceed 100 characters."
        )
        String nationality,

        @Size(
                max = 50,
                message = "House number cannot exceed 50 characters."
        )
        String houseNo,

        @Size(
                max = 150,
                message = "Street cannot exceed 150 characters."
        )
        String street,

        @Size(
                max = 100,
                message = "Village cannot exceed 100 characters."
        )
        String village,

        @Size(
                max = 100,
                message = "Town or city cannot exceed 100 characters."
        )
        String townCity,

        @Size(
                max = 100,
                message = "District cannot exceed 100 characters."
        )
        String district,

        @Size(
                max = 100,
                message = "State or region cannot exceed 100 characters."
        )
        String state,

        @Size(
                max = 100,
                message = "Country cannot exceed 100 characters."
        )
        String country,

        @Size(
                max = 20,
                message = "Postal code cannot exceed 20 characters."
        )
        String postalCode
) {
}