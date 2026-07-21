package com.erp.montfortuganda.employee.dto.request;

import jakarta.validation.constraints.AssertTrue;

/**
 * Request used by Branch Admin to generate a new temporary password for an
 * existing Employee login account.
 *
 * <p>The new temporary password is never returned through the API. It must be
 * delivered to the Employee's official email address.</p>
 */
@SuppressWarnings("unused")
public record EmployeeTemporaryPasswordRequest(

        boolean sendEmail
) {

    @AssertTrue(
            message =
                    "Temporary password must be sent to the Employee's official email."
    )
    public boolean isEmailDeliveryRequired() {
        return sendEmail;
    }
}
