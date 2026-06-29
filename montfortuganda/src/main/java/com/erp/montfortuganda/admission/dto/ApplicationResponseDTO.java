package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ApplicationResponseDTO {

    private Long applicationId;
    private String applicationNo;
    private ErpApplication.ApplicationStatus applicationStatus;

    // We flatten some fields for the frontend so it doesn't need complex objects
    private String branchName;
    private String className;

    private String firstName;
    private String middleName;
    private String lastName;
    private ErpApplication.Gender gender;
    private LocalDate dateOfBirth;

    private String nationality;
    private ErpApplication.AdmissionType admissionType;
    private String previousSchool;

    private String guardianName;
    private String guardianMobile;
    private String guardianEmail;

    private LocalDateTime createdAt;
}