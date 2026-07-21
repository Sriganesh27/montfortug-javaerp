package com.erp.montfortuganda.employee.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;

/**
 * Login-account options submitted with an Employee registration request.

 * The DTO is consumed by Jakarta Bean Validation and will be referenced by
 * EmployeeRegistrationRequest. Suppressions avoid false-positive IDE warnings
 * while the dependent DTOs are added one file at a time.
 */
@SuppressWarnings("unused")
public record EmployeeAccountRequest(

        boolean generateLogin,

        @Positive(message = "Employee login role ID must be greater than zero.")
        Long roleId,

        boolean sendEmail
) {

    @AssertTrue(
            message = "A valid role must be selected when employee login creation is enabled."
    )
    public boolean isRoleSelectionValid() {
        return !generateLogin
                || roleId != null;
    }

    @AssertTrue(
            message = "Login credentials can be emailed only when employee login creation is enabled."
    )
    public boolean isEmailSelectionValid() {
        return !sendEmail
                || generateLogin;
    }
}