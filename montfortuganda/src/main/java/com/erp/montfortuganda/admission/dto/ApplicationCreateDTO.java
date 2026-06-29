package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ApplicationCreateDTO {

    // Ensures malicious users cannot overwrite application_id or status
    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Academic Year ID is required")
    private Long academicYearId;

    @NotNull(message = "Class ID is required")
    private Long branchClassId;

    @NotBlank(message = "First Name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    @NotNull(message = "Gender is required")
    private ErpApplication.Gender gender;

    private LocalDate dateOfBirth;

    private Long religionId;
    private Long bloodGroupId;
    private Long categoryId;

    private String nationality = "Uganda";
    private ErpApplication.AdmissionType admissionType = ErpApplication.AdmissionType.NEW;
    private String previousSchool;

    private String guardianName;
    private String guardianMobile;
    private String guardianEmail;
}