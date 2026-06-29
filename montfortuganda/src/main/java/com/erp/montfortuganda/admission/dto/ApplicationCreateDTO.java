package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ApplicationCreateDTO {

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

    // Address Fields
    private String addressHouse;
    private String addressStreet;
    private String addressVillage;
    private String addressDistrict;
    private String addressState;
    private String addressPostal;
    private String addressCountry;

    // Father Fields
    private String fatherName;
    private Integer fatherAge;
    private String fatherContact;
    private String fatherEducation;
    private String fatherOccupation;
    private String fatherEmail;

    // Mother Fields
    private String motherName;
    private Integer motherAge;
    private String motherContact;
    private String motherEducation;
    private String motherOccupation;
    private String motherEmail;

    // Guardian Fields
    private String guardianName;
    private String guardianMobile;
    private String guardianEmail;
    private Integer guardianAge;
    private String guardianEducation;
    private String guardianOccupation;
    private String guardianRelation;
    private String guardianLocation;

    // Academic Fields
    private String previousSchool;
    private String formerSchoolCode;
    private String formerSchoolLin;
    private String pleRef;
    private Double pleScore;
    private String uceRef;
    private Double uceScore;
    private String subjectMarks;
    private String scholarshipStatus;
    private String moreInfo;
}