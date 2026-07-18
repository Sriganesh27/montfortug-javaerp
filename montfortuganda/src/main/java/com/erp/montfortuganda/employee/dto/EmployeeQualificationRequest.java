package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.QualificationLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeQualificationRequest {

    private Long employeeQualificationId;

    @NotNull(message = "Qualification level is required")
    private QualificationLevel employeeQualificationLevel;

    /**
     * Used only when employeeQualificationLevel is OTHER.
     */
    @Size(max = 255)
    private String customLevel;

    /**
     * Real qualification name.
     *
     * Examples:
     * Bachelor of Education
     * Master of Computer Applications
     * Uganda Certificate of Education
     */
    @NotBlank(message = "Qualification name is required")
    @Size(max = 255)
    private String employeeQualificationName;

    @Size(max = 255)
    private String employeeQualificationSpecialization;

    @NotBlank(message = "Institution name is required")
    @Size(max = 255)
    private String employeeQualificationInstitutionName;

    @Size(max = 255)
    private String employeeQualificationBoardUniversity;

    @Size(max = 100)
    private String employeeQualificationCountry;

    @Min(value = 1900, message = "Invalid start year")
    @Max(value = 2100, message = "Invalid start year")
    private Integer employeeQualificationStartYear;

    @NotNull(message = "Completion year is required")
    @Min(value = 1900, message = "Invalid completion year")
    @Max(value = 2100, message = "Invalid completion year")
    private Integer employeeQualificationCompletionYear;

    @Min(value = 0, message = "Duration cannot be negative")
    @Max(
            value = 600,
            message = "Duration exceeds maximum realistic limit"
    )
    private Integer employeeQualificationDurationMonths;

    /**
     * Maps to employee_qualification_grade.
     */
    @Size(max = 50)
    private String employeeQualificationGrade;

    /**
     * Maps to qualification_grade.
     * Keep optional until the business meaning is finalized.
     */
    @Size(max = 100)
    private String qualificationGrade;

    @DecimalMin(
            value = "0.0",
            message = "Percentage cannot be negative"
    )
    @DecimalMax(
            value = "100.0",
            message = "Percentage cannot exceed 100"
    )
    private BigDecimal employeeQualificationPercentage;

    @DecimalMin(
            value = "0.0",
            message = "CGPA cannot be negative"
    )
    @DecimalMax(
            value = "10.0",
            message = "CGPA cannot exceed 10"
    )
    private BigDecimal employeeQualificationCgpa;

    @Size(max = 100)
    private String employeeQualificationCertificateNumber;

    @Size(max = 100)
    private String employeeQualificationRegistrationNumber;

    @Size(max = 1000)
    private String employeeQualificationRemarks;

    private Boolean active = true;

    private String fileData;

    private String fileName;
}