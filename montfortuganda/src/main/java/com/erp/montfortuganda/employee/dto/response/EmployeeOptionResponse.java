package com.erp.montfortuganda.employee.dto.response;

/**
 * Lightweight branch-owned Employee option used by dropdowns such as
 * Reporting Manager selection.
 *
 * <p>The frontend displays readable Employee information while submitting
 * only {@code employeeId} back to the backend.</p>
 */
@SuppressWarnings("unused")
public record EmployeeOptionResponse(

        Long employeeId,

        String employeeNo,

        String fullName,

        Long departmentId,

        String departmentName,

        Long designationId,

        String designationName
) {
}