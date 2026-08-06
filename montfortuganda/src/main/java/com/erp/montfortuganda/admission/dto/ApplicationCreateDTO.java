package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

@Data
public class ApplicationCreateDTO {

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Academic Year ID is required")
    @Positive(message = "Academic Year ID must be valid")
    private Long academicYearId;

    @NotNull(message = "Joining Term ID is required")
    @Positive(message = "Joining Term ID must be valid")
    private Long joiningTermId;

    @NotNull(message = "Class ID is required")
    private Long branchClassId;
    @NotBlank(message = "Primary Email is required for notifications")
    private String primaryEmail;

    @NotBlank(message = "Primary Mobile Number is required for SMS alerts")
    private String primaryMobile;
    @NotBlank(message = "First Name is required")
    private String firstName;
    private String middleName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    @NotNull(message = "Gender is required")
    private ErpApplication.Gender gender;
    private LocalDate dateOfBirth;
    @PastOrPresent(message = "Date of Registration cannot be in the future")
    private LocalDate dateOfRegistration;

    private String nationality = "Uganda";
    private ErpApplication.AdmissionType admissionType = ErpApplication.AdmissionType.NEW;

    // Address Fields
    private String addressHouse;
    private String addressStreet;
    private String addressVillage;
    private String addressDistrict;
    private String addressState;
    private String addressPostal;

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

    /**
     * Legacy display value retained temporarily for compatibility with
     * existing application print/email code. Public submission must use
     * {@link #joiningTermId}; the backend will derive this text from the
     * validated academic-term master record.
     */
    private String term;
}
