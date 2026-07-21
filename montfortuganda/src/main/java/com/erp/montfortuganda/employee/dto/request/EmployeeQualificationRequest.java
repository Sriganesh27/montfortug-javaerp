package com.erp.montfortuganda.employee.dto.request;

import com.erp.montfortuganda.employee.enums.QualificationLevel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Qualification details submitted with an Employee registration or update
 * request.
 * File content is received as Base64 from the existing Employee frontend.
 * The permanent private-storage path is assigned by EmployeeFileService and
 * is never accepted directly from the client.
 */
@SuppressWarnings("unused")
public record EmployeeQualificationRequest(

        @Positive(message = "Employee qualification ID must be greater than zero.")
        Long employeeQualificationId,

        @NotNull(message = "Qualification level is required.")
        QualificationLevel employeeQualificationLevel,

        @Size(
                max = 255,
                message = "Custom qualification level cannot exceed 255 characters."
        )
        String customLevel,

        @NotBlank(message = "Qualification name is required.")
        @Size(
                max = 255,
                message = "Qualification name cannot exceed 255 characters."
        )
        String employeeQualificationName,

        @Size(
                max = 255,
                message = "Qualification specialization cannot exceed 255 characters."
        )
        String employeeQualificationSpecialization,

        @NotBlank(message = "Qualification institution name is required.")
        @Size(
                max = 255,
                message = "Qualification institution name cannot exceed 255 characters."
        )
        String employeeQualificationInstitutionName,

        @Size(
                max = 100,
                message = "Qualification grade cannot exceed 100 characters."
        )
        String qualificationGrade,

        @Size(
                max = 255,
                message = "Board or university name cannot exceed 255 characters."
        )
        String employeeQualificationBoardUniversity,

        @Size(
                max = 100,
                message = "Qualification country cannot exceed 100 characters."
        )
        String employeeQualificationCountry,

        @NotNull(message = "Qualification start year is required.")
        @Min(
                value = 1900,
                message = "Qualification start year must be 1900 or later."
        )
        @Max(
                value = 2100,
                message = "Qualification start year cannot exceed 2100."
        )
        Integer employeeQualificationStartYear,

        @NotNull(message = "Qualification completion year is required.")
        @Min(
                value = 1900,
                message = "Qualification completion year must be 1900 or later."
        )
        @Max(
                value = 2100,
                message = "Qualification completion year cannot exceed 2100."
        )
        Integer employeeQualificationCompletionYear,

        @PositiveOrZero(
                message = "Qualification duration cannot be negative."
        )
        Integer employeeQualificationDurationMonths,

        @Size(
                max = 50,
                message = "Employee qualification grade cannot exceed 50 characters."
        )
        String employeeQualificationGrade,

        @DecimalMin(
                value = "0.00",
                message = "Qualification percentage cannot be negative."
        )
        @DecimalMax(
                value = "100.00",
                message = "Qualification percentage cannot exceed 100."
        )
        BigDecimal employeeQualificationPercentage,

        @DecimalMin(
                value = "0.00",
                message = "Qualification CGPA cannot be negative."
        )
        BigDecimal employeeQualificationCgpa,

        @Size(
                max = 100,
                message = "Qualification certificate number cannot exceed 100 characters."
        )
        String employeeQualificationCertificateNumber,

        @Size(
                max = 100,
                message = "Qualification registration number cannot exceed 100 characters."
        )
        String employeeQualificationRegistrationNumber,

        @Size(
                max = 5000,
                message = "Qualification remarks cannot exceed 5000 characters."
        )
        String employeeQualificationRemarks,

        @NotNull(message = "Qualification active status is required.")
        Boolean employeeQualificationActive,

        String fileData,

        @Size(
                max = 255,
                message = "Qualification file name cannot exceed 255 characters."
        )
        String fileName,

        @Size(
                max = 100,
                message = "Qualification file content type cannot exceed 100 characters."
        )
        String contentType,

        @Positive(
                message = "Qualification file size must be greater than zero."
        )
        Long fileSize
) {

    @AssertTrue(
            message = "Custom qualification level is required when qualification level is OTHER."
    )
    public boolean isCustomLevelValid() {
        return employeeQualificationLevel != QualificationLevel.OTHER
                || hasText(customLevel);
    }

    @AssertTrue(
            message = "Specialization or subject combination is required for Senior Secondary."
    )
    public boolean isSpecializationValid() {
        return employeeQualificationLevel
                != QualificationLevel.SENIOR_SECONDARY
                || hasText(
                employeeQualificationSpecialization
        );
    }

    @AssertTrue(
            message = "Qualification completion year cannot be earlier than the start year."
    )
    public boolean isYearRangeValid() {
        return employeeQualificationStartYear == null
                || employeeQualificationCompletionYear == null
                || employeeQualificationCompletionYear
                >= employeeQualificationStartYear;
    }

    @AssertTrue(
            message = "Qualification file name, content type and size are required when a file is uploaded."
    )
    public boolean isFileMetadataValid() {
        if (!hasText(fileData)) {
            return !hasText(fileName)
                    && !hasText(contentType)
                    && fileSize == null;
        }

        return hasText(fileName)
                && hasText(contentType)
                && fileSize != null
                && fileSize > 0;
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}