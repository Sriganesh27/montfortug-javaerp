package com.erp.montfortuganda.employee.dto.request;

import com.erp.montfortuganda.employee.enums.ExperienceEmploymentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Previous-employment details submitted with an Employee registration or
 * update request.

 * The database contains both employee_experience_type and
 * employee_experience_employment_type. The browser supplies only
 * employeeExperienceEmploymentType; the backend mapper stores that value in
 * both columns so the user is not asked for the same information twice.

 * Experience-certificate and relieving-letter uploads are separate because
 * the entity and table store them separately. Permanent private file paths,
 * verification data, total-month calculations, audit fields and version
 * changes are controlled by the backend.
 */
@SuppressWarnings("unused")
public record EmployeeExperienceRequest(

        @Positive(message = "Employee experience ID must be greater than zero.")
        Long employeeExperienceId,

        @NotBlank(message = "Experience organisation name is required.")
        @Size(
                max = 255,
                message = "Experience organisation name cannot exceed 255 characters."
        )
        String employeeExperienceCompanyName,

        @Size(
                max = 255,
                message = "Experience company address cannot exceed 255 characters."
        )
        String employeeExperienceCompanyAddress,

        @Size(
                max = 100,
                message = "Experience company country cannot exceed 100 characters."
        )
        String employeeExperienceCompanyCountry,

        @Size(
                max = 100,
                message = "Experience company state cannot exceed 100 characters."
        )
        String employeeExperienceCompanyState,

        @Size(
                max = 100,
                message = "Experience company district cannot exceed 100 characters."
        )
        String employeeExperienceCompanyDistrict,

        @Size(
                max = 150,
                message = "Experience designation cannot exceed 150 characters."
        )
        String employeeExperienceDesignation,

        @Size(
                max = 150,
                message = "Experience department cannot exceed 150 characters."
        )
        String employeeExperienceDepartment,

        @NotNull(message = "Experience employment type is required.")
        ExperienceEmploymentType employeeExperienceEmploymentType,

        @NotNull(message = "Experience start date is required.")
        LocalDate employeeExperienceStartDate,

        LocalDate employeeExperienceEndDate,

        @NotNull(message = "Current-job selection is required.")
        Boolean employeeExperienceCurrentJob,

        @DecimalMin(
                value = "0.00",
                message = "Experience salary cannot be negative."
        )
        BigDecimal employeeExperienceSalary,

        @Size(
                max = 10,
                message = "Experience salary currency cannot exceed 10 characters."
        )
        String employeeExperienceCurrency,

        @Size(
                max = 255,
                message = "Experience supervisor name cannot exceed 255 characters."
        )
        String employeeExperienceSupervisorName,

        @Size(
                max = 100,
                message = "Experience supervisor contact cannot exceed 100 characters."
        )
        String employeeExperienceSupervisorContact,

        @Size(
                max = 255,
                message = "Reason for leaving cannot exceed 255 characters."
        )
        String employeeExperienceReasonForLeaving,

        String employeeExperienceResponsibilities,

        String employeeExperienceAchievements,

        Boolean employeeExperienceActive,

        String employeeExperienceRemarks,

        String experienceCertificateFileData,

        @Size(
                max = 255,
                message = "Experience-certificate file name cannot exceed 255 characters."
        )
        String experienceCertificateFileName,

        @Size(
                max = 100,
                message = "Experience-certificate content type cannot exceed 100 characters."
        )
        String experienceCertificateContentType,

        @Positive(
                message = "Experience-certificate file size must be greater than zero."
        )
        Long experienceCertificateFileSize,

        String relievingLetterFileData,

        @Size(
                max = 255,
                message = "Relieving-letter file name cannot exceed 255 characters."
        )
        String relievingLetterFileName,

        @Size(
                max = 100,
                message = "Relieving-letter content type cannot exceed 100 characters."
        )
        String relievingLetterContentType,

        @Positive(
                message = "Relieving-letter file size must be greater than zero."
        )
        Long relievingLetterFileSize
) {

    @AssertTrue(
            message = "Experience end date cannot be earlier than the start date."
    )
    public boolean isDateRangeValid() {
        return employeeExperienceStartDate == null
                || employeeExperienceEndDate == null
                || !employeeExperienceEndDate.isBefore(
                employeeExperienceStartDate
        );
    }

    @AssertTrue(
            message = "A current job cannot have an end date, and a previous job requires an end date."
    )
    public boolean isCurrentJobValid() {
        return Boolean.TRUE.equals(
                employeeExperienceCurrentJob
        ) == (employeeExperienceEndDate == null);
    }

    @AssertTrue(
            message = "Experience salary and currency must be supplied together."
    )
    public boolean isSalaryDataValid() {
        return (
                employeeExperienceSalary == null
                        && !hasText(employeeExperienceCurrency)
        )
                || (
                employeeExperienceSalary != null
                        && hasText(employeeExperienceCurrency)
        );
    }

    @AssertTrue(
            message = "Experience-certificate file data and metadata must be supplied together."
    )
    public boolean isExperienceCertificateMetadataValid() {
        return isCompleteOptionalFile(
                experienceCertificateFileData,
                experienceCertificateFileName,
                experienceCertificateContentType,
                experienceCertificateFileSize
        );
    }

    @AssertTrue(
            message = "Relieving-letter file data and metadata must be supplied together."
    )
    public boolean isRelievingLetterMetadataValid() {
        return isCompleteOptionalFile(
                relievingLetterFileData,
                relievingLetterFileName,
                relievingLetterContentType,
                relievingLetterFileSize
        );
    }

    private boolean isCompleteOptionalFile(
            String fileData,
            String fileName,
            String contentType,
            Long fileSize
    ) {
        boolean anyValueSupplied =
                hasText(fileData)
                        || hasText(fileName)
                        || hasText(contentType)
                        || fileSize != null;

        return !anyValueSupplied
                || (
                hasText(fileData)
                        && hasText(fileName)
                        && hasText(contentType)
                        && fileSize != null
        );
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}