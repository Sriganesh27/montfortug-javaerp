package com.erp.montfortuganda.employee.dto.request;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.enums.Gender;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Branch-scoped Employee search filters.

 * The authenticated user's branch is always applied by the backend and is not
 * accepted from the browser. Pagination and sorting remain query parameters.
 */
@SuppressWarnings("unused")
public record EmployeeSearchRequest(

        @Size(
                max = 255,
                message = "Employee search keyword cannot exceed 255 characters."
        )
        String keyword,

        @Size(
                max = 50,
                message = "Employee number cannot exceed 50 characters."
        )
        String employeeNo,

        @Size(
                max = 150,
                message = "Official email cannot exceed 150 characters."
        )
        String officialEmail,

        @Size(
                max = 30,
                message = "Mobile number cannot exceed 30 characters."
        )
        String mobileNo,

        @Positive(message = "Department ID must be greater than zero.")
        Long departmentId,

        @Positive(message = "Designation ID must be greater than zero.")
        Long designationId,

        @Positive(message = "Reporting-manager Employee ID must be greater than zero.")
        Long reportingManagerId,

        EmployeeCategory employeeCategory,

        EmployeeType employeeType,

        EmploymentMode employmentMode,

        EmploymentStatus employmentStatus,

        Gender gender,

        LocalDate dateOfBirthFrom,

        LocalDate dateOfBirthTo,

        LocalDate joiningDateFrom,

        LocalDate joiningDateTo,

        LocalDate employmentEndDateFrom,

        LocalDate employmentEndDateTo,

        Boolean loginEnabled,

        Boolean active
) {

    @AssertTrue(
            message = "Date-of-birth start date cannot be later than the end date."
    )
    public boolean isDateOfBirthRangeValid() {
        return isValidRange(
                dateOfBirthFrom,
                dateOfBirthTo
        );
    }

    @AssertTrue(
            message = "Joining-date start date cannot be later than the end date."
    )
    public boolean isJoiningDateRangeValid() {
        return isValidRange(
                joiningDateFrom,
                joiningDateTo
        );
    }

    @AssertTrue(
            message = "Employment-end start date cannot be later than the end date."
    )
    public boolean isEmploymentEndDateRangeValid() {
        return isValidRange(
                employmentEndDateFrom,
                employmentEndDateTo
        );
    }

    private boolean isValidRange(
            LocalDate from,
            LocalDate to
    ) {
        return from == null
                || to == null
                || !from.isAfter(to);
    }
}