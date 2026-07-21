package com.erp.montfortuganda.employee.dto.response;

import com.erp.montfortuganda.employee.enums.ExperienceEmploymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Complete Employee experience data returned by authenticated, branch-scoped
 * Employee APIs.

 * Private experience-certificate and relieving-letter storage paths are never
 * exposed. Files are opened only through secured Employee document endpoints.
 */
@SuppressWarnings("unused")
public record EmployeeExperienceResponse(

        Long employeeExperienceId,

        Long employeeId,

        ExperienceEmploymentType employeeExperienceType,

        String employeeExperienceCompanyName,

        String employeeExperienceCompanyAddress,

        String employeeExperienceCompanyCountry,

        String employeeExperienceCompanyState,

        String employeeExperienceCompanyDistrict,

        String employeeExperienceDesignation,

        String employeeExperienceDepartment,

        ExperienceEmploymentType employeeExperienceEmploymentType,

        LocalDate employeeExperienceStartDate,

        LocalDate employeeExperienceEndDate,

        Boolean employeeExperienceCurrentJob,

        Integer employeeExperienceTotalMonths,

        BigDecimal employeeExperienceSalary,

        String employeeExperienceCurrency,

        String employeeExperienceSupervisorName,

        String employeeExperienceSupervisorContact,

        String employeeExperienceReasonForLeaving,

        String employeeExperienceResponsibilities,

        String employeeExperienceAchievements,

        Boolean employeeExperienceExperienceCertificateAvailable,

        Boolean employeeExperienceRelievingLetterAvailable,

        Boolean employeeExperienceVerified,

        Integer employeeExperienceVerifiedById,

        String employeeExperienceVerifiedByUsername,

        LocalDateTime employeeExperienceVerifiedAt,

        Boolean employeeExperienceActive,

        String employeeExperienceRemarks,

        Long version,

        String createdBy,

        LocalDateTime createdAt,

        String updatedBy,

        LocalDateTime updatedAt
) {
}