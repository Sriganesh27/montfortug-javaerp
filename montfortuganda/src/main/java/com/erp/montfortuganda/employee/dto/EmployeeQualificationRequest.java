// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeQualificationRequest.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.QualificationLevel;
import com.erp.montfortuganda.employee.enums.QualificationStatus;
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
import java.time.LocalDate;

@Data
public class    EmployeeQualificationRequest {

    // --- IDENTITY ---
    private Long employeeQualificationId;

    @Size(max = 50)
    private String employeeQualificationCode;

    // --- QUALIFICATION ---
    @NotNull(message = "Qualification level is required")
    private QualificationLevel employeeQualificationLevel;

    @NotBlank(message = "Qualification name is required")
    @Size(max = 255)
    @Pattern(regexp = "^[A-Za-z0-9 .,'()&/\\-]+$", message = "Invalid qualification name")
    private String employeeQualificationName;

    @Size(max = 255)
    private String employeeQualificationSpecialization;

    // --- INSTITUTE ---
    @NotBlank(message = "Institution name is required")
    @Size(max = 255)
    private String employeeQualificationInstitutionName;

    @Size(max = 255)
    private String employeeQualificationBoardUniversity;

    @Size(max = 100)
    private String employeeQualificationCountry;

    // --- TIMELINE ---
    @Min(value = 1900, message = "Invalid start year")
    @Max(value = 2100, message = "Invalid start year")
    private Integer employeeQualificationStartYear;

    @NotNull(message = "Completion year is required")
    @Min(value = 1900, message = "Invalid completion year")
    @Max(value = 2100, message = "Invalid completion year")
    private Integer employeeQualificationCompletionYear;

    @Min(value = 0, message = "Duration cannot be negative")
    @Max(value = 600, message = "Duration exceeds maximum realistic limit")
    private Integer employeeQualificationDurationMonths;

    private LocalDate employeeQualificationAwardDate;
    private LocalDate employeeQualificationExpiryDate;

    // --- RESULT ---
    @Size(max = 50)
    private String employeeQualificationGrade;

    @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    private BigDecimal employeeQualificationPercentage;

    @DecimalMin(value = "0.0", message = "CGPA cannot be negative")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10")
    private BigDecimal employeeQualificationCgpa;

    // --- CERTIFICATE ---
    @Size(max = 100)
    private String employeeQualificationCertificateNumber;

    @Size(max = 100)
    private String employeeQualificationRegistrationNumber;

    // --- VERIFICATION LIFECYCLE ---
    @NotNull(message = "Qualification status is required")
    private QualificationStatus status = QualificationStatus.PENDING;

    private LocalDate employeeQualificationVerificationDate;

    private Long verifiedByUserId;

    // --- ATTACHMENT & REMARKS ---
    @Size(max = 500)
    private String employeeQualificationAttachmentPath;

    @Size(max = 1000)
    private String employeeQualificationRemarks;

    private String qualificationGrade;
    private String customLevel;

    // --- SYSTEM ---
    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder = 1;

    private Integer displayYear;

    private Boolean active = true;

    private Boolean deleted = false;
    private String fileData;
    private String fileName;
}