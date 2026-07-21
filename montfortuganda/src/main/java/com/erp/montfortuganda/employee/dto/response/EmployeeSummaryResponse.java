package com.erp.montfortuganda.employee.dto.response;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lightweight Employee data returned by branch-scoped search and list APIs.

 * This response contains the fields required by the Employee listing screen
 * without exposing private file-storage paths or unnecessary nested records.
 * The profilePhotoUrl, when present, must point to a secured backend endpoint.
 */
@SuppressWarnings("unused")
public record EmployeeSummaryResponse(

        Long employeeId,

        String employeeNo,

        String title,

        String fullName,

        Gender gender,

        Integer branchId,

        String branchName,

        String schoolCode,

        Long departmentId,

        String departmentCode,

        String departmentName,

        Long designationId,

        String designationCode,

        String designationName,

        Long reportingManagerId,

        String reportingManagerEmployeeNo,

        String reportingManagerName,

        EmployeeCategory employeeCategory,

        EmployeeType employeeType,

        EmploymentMode employmentMode,

        EmploymentStatus employmentStatus,

        LocalDate joiningDate,

        String officialEmail,

        String mobileNo,

        Boolean loginEnabled,

        Integer userId,

        String username,

        String loginRole,

        Boolean profilePhotoAvailable,

        String profilePhotoUrl,

        Boolean active,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}