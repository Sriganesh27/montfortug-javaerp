package com.erp.montfortuganda.employee.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request used by Branch Admin to create a login account for an existing
 * Employee who was registered without one.
 */
@SuppressWarnings("unused")
public record EmployeeLoginAccountRequest(

        @NotNull(message = "Employee login role is required.")
        @Positive(message = "Employee login role ID must be greater than zero.")
        Long roleId,

        boolean sendEmail
) {
}
