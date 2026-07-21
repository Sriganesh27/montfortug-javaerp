package com.erp.montfortuganda.employee.dto.request;

import com.erp.montfortuganda.employee.enums.ContactRelationship;
import com.erp.montfortuganda.employee.enums.ContactType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Emergency, next-of-kin, guardian, reference, or other contact submitted
 * with an Employee registration or update request.
 */
@SuppressWarnings("unused")
public record EmployeeContactRequest(

        @Positive(message = "Employee contact ID must be greater than zero.")
        Long employeeContactId,

        @NotBlank(message = "Employee contact name is required.")
        @Size(
                max = 255,
                message = "Employee contact name cannot exceed 255 characters."
        )
        String employeeContactName,

        @NotNull(message = "Employee contact relationship is required.")
        ContactRelationship employeeContactRelationship,

        @NotNull(message = "Employee contact type is required.")
        ContactType employeeContactType,

        @NotBlank(message = "Employee contact mobile number is required.")
        @Size(
                max = 30,
                message = "Employee contact mobile number cannot exceed 30 characters."
        )
        String employeeContactMobile,

        @Size(
                max = 30,
                message = "Alternate mobile number cannot exceed 30 characters."
        )
        String employeeContactAlternateMobile,

        @Email(message = "Employee contact email must be valid.")
        @Size(
                max = 150,
                message = "Employee contact email cannot exceed 150 characters."
        )
        String employeeContactEmail,

        @Size(
                max = 100,
                message = "Employee contact country cannot exceed 100 characters."
        )
        String employeeContactCountry,

        @Size(
                max = 100,
                message = "Employee contact state cannot exceed 100 characters."
        )
        String employeeContactState,

        @Size(
                max = 100,
                message = "Employee contact district cannot exceed 100 characters."
        )
        String employeeContactDistrict,

        @Size(
                max = 150,
                message = "Employee contact village cannot exceed 150 characters."
        )
        String employeeContactVillage,

        @Size(
                max = 255,
                message = "Employee contact street cannot exceed 255 characters."
        )
        String employeeContactStreet,

        @Size(
                max = 30,
                message = "Employee contact postal code cannot exceed 30 characters."
        )
        String employeeContactPostalCode,

        @Size(
                max = 150,
                message = "Employee contact occupation cannot exceed 150 characters."
        )
        String employeeContactOccupation,

        @Size(
                max = 255,
                message = "Employee contact workplace cannot exceed 255 characters."
        )
        String employeeContactWorkplace,

        @NotNull(message = "Primary-contact selection is required.")
        Boolean employeeContactIsPrimary,

        @NotNull(message = "Emergency-contact selection is required.")
        Boolean employeeContactIsEmergency,

        @NotNull(message = "Employee contact active status is required.")
        Boolean employeeContactActive,

        @Size(
                max = 5000,
                message = "Employee contact remarks cannot exceed 5000 characters."
        )
        String employeeContactRemarks
) {
}