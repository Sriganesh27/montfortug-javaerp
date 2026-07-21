package com.erp.montfortuganda.employee.dto.response;

import com.erp.montfortuganda.auth.entity.CredentialDeliveryStatus;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Complete Employee data returned by the authenticated, branch-scoped detail
 * API.

 * Private file-system paths, generated storage names, password hashes and
 * temporary passwords are never exposed. File access is provided through
 * secured backend URLs after branch and Employee ownership checks.
 */
@SuppressWarnings("unused")
public record EmployeeDetailResponse(

        Long employeeId,

        String employeeNo,

        String title,

        String firstName,

        String middleName,

        String lastName,

        String fullName,

        Gender gender,

        LocalDate dateOfBirth,

        String maritalStatus,

        String bloodGroup,

        String religion,

        String subReligion,

        Boolean profilePhotoAvailable,

        String profilePhotoUrl,

        Boolean signatureFileAvailable,

        String signatureFileUrl,

        String nationality,

        String nationalId,

        String passportNo,

        LocalDate passportExpiryDate,

        String tinNumber,

        String workPermitNumber,

        LocalDate workPermitExpiryDate,

        String officialEmail,

        String personalEmail,

        String mobileNo,

        String alternateMobile,

        String addressCountry,

        String addressState,

        String addressDistrict,

        String addressCounty,

        String addressSubCounty,

        String addressParish,

        String addressVillage,

        String addressStreet,

        String postalCode,

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

        LocalDate probationEndDate,

        LocalDate confirmationDate,

        LocalDate retirementDate,

        LocalDate resignationDate,

        LocalDate terminationDate,

        LocalDate employmentEndDate,

        String exitReason,

        String skills,

        String languagesSpoken,

        String employeeRemarks,

        Boolean loginEnabled,

        Integer userId,

        String username,

        String loginRole,

        List<String> loginRoles,

        String loginStatus,

        Boolean mustChangePassword,

        LocalDateTime temporaryPasswordExpiresAt,

        CredentialDeliveryStatus credentialDeliveryStatus,

        LocalDateTime credentialsSentAt,

        Integer credentialDeliveryAttempts,

        List<EmployeeContactResponse> contacts,

        List<EmployeeQualificationResponse> qualifications,

        List<EmployeeExperienceResponse> experiences,

        List<EmployeeDocumentResponse> documents,

        Boolean active,

        Long version,

        String createdBy,

        LocalDateTime createdAt,

        String updatedBy,

        LocalDateTime updatedAt
) {
}