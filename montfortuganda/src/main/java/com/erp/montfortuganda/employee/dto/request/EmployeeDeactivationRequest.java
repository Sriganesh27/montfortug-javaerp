package com.erp.montfortuganda.employee.dto.request;

import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request used to deactivate an Employee without deleting database records.
 */
@SuppressWarnings("unused")
public record EmployeeDeactivationRequest(

        @NotNull(message = "Final employment status is required.")
        EmploymentStatus employmentStatus,

        @NotNull(message = "Deactivation effective date is required.")
        @PastOrPresent(
                message =
                        "Deactivation effective date cannot be in the future."
        )
        LocalDate effectiveDate,

        @NotBlank(message = "Employee exit reason is required.")
        @Size(
                max = 10000,
                message =
                        "Employee exit reason cannot exceed 10000 characters."
        )
        String exitReason
) {

    @AssertTrue(
            message =
                    "Final employment status must be RESIGNED, RETIRED or TERMINATED."
    )
    public boolean isFinalEmploymentStatusValid() {
        return employmentStatus == null
                || employmentStatus == EmploymentStatus.RESIGNED
                || employmentStatus == EmploymentStatus.RETIRED
                || employmentStatus == EmploymentStatus.TERMINATED;
    }
}