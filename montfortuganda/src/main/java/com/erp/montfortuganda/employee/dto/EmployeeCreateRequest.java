// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeCreateRequest.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EmployeeCreateRequest {

    // --- RELATIONSHIPS ---
    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Designation is required")
    private Long designationId;

    private Long reportingManagerId;

    // --- PERSONAL INFO ---
    @Size(max = 20)
    private String title;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Pattern(regexp = "^[A-Za-z .'-]+$", message = "Invalid first name format")
    private String firstName;

    @Size(max = 100)
    @Pattern(regexp = "^[A-Za-z .'-]+$", message = "Invalid middle name format")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Pattern(regexp = "^[A-Za-z .'-]+$", message = "Invalid last name format")
    private String lastName;

    private Gender gender;

    private LocalDate dateOfBirth;

    @Size(max = 100)
    private String nationality;

    @Size(max = 50)
    private String nationalId;

    @Size(max = 50)
    private String passportNo;

    @Size(max = 50)
    private String tinNumber;

    @Size(max = 50)
    private String maritalStatus;

    @Size(max = 20)
    private String bloodGroup;

    @Size(max = 100)
    private String religion;

    // --- CONTACT INFO ---
    @Email(message = "Invalid official email format")
    @Size(max = 150)
    private String officialEmail;

    @Email(message = "Invalid personal email format")
    @Size(max = 150)
    private String personalEmail;

    @Pattern(regexp = "^[+0-9\\-\\s]{7,20}$", message = "Invalid mobile number")
    private String mobileNo;

    @Pattern(regexp = "^[+0-9\\-\\s]{7,20}$", message = "Invalid alternate mobile number")
    private String alternateMobile;

    // --- ADDRESS INFO ---
    @Size(max = 100)
    private String addressCountry;

    @Size(max = 100)
    private String addressState;

    @Size(max = 100)
    private String addressDistrict;

    @Size(max = 150)
    private String addressVillage;

    @Size(max = 255)
    private String addressStreet;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String addressCounty;

    @Size(max = 100)
    private String addressSubCounty;

    @Size(max = 100)
    private String addressParish;

    private String skills;
    private String languagesSpoken;
    private String subReligion;

    // --- EMPLOYMENT INFO ---
    @NotNull(message = "Employee category is required")
    private EmployeeCategory employeeCategory;

    @NotNull(message = "Employee type is required")
    private EmployeeType employeeType;

    @NotNull(message = "Employment mode is required")
    private EmploymentMode employmentMode;

    @NotNull(message = "Employment status is required")
    private EmploymentStatus employmentStatus;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private LocalDate probationEndDate;
    private LocalDate confirmationDate;

    @Size(max = 100)
    private String workPermitNumber;

    private LocalDate workPermitExpiryDate;
    private LocalDate passportExpiryDate;

    // --- OFFBOARDING & REMARKS ---
    private LocalDate retirementDate;
    private LocalDate resignationDate;
    private LocalDate terminationDate;
    private LocalDate employmentEndDate;

    @Size(max = 500)
    private String exitReason;

    @Size(max = 1000)
    private String employeeRemarks;

    // --- ACCOUNT INFO ---
    @Valid
    private EmployeeAccountRequest accountRequest;

    // --- CHILD ENTITIES (Nested Collections) ---
    @Valid
    private List<EmployeeContactRequest> contacts;

    @Valid
    private List<EmployeeQualificationRequest> qualifications;

    @Valid
    private List<EmployeeExperienceRequest> experiences;

    @Valid
    private List<EmployeeDocumentRequest> documents;
}
