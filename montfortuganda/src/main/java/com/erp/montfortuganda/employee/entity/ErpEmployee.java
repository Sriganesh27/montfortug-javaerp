// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployee.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.*;
import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_employees", indexes = {
        @Index(name = "idx_emp_branch", columnList = "branch_id"),
        @Index(name = "idx_emp_department", columnList = "department_id"),
        @Index(name = "idx_emp_designation", columnList = "designation_id"),
        @Index(name = "idx_emp_status", columnList = "employment_status"),
        @Index(name = "idx_emp_category", columnList = "employee_category")
})
public class ErpEmployee extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    private ErpEmployee reportingManager;

    @Column(name = "employee_no", nullable = false, unique = true, length = 50)
    private String employeeNo;

    @Column(length = 20)
    private String title;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_photo", length = 500)
    private String profilePhoto;

    @Column(name = "signature_file", length = 500)
    private String signatureFile;

    @Column(length = 100)
    private String nationality;

    @Column(name = "national_id", length = 100)
    private String nationalId;

    @Column(name = "passport_no", length = 100)
    private String passportNo;

    @Column(name = "tin_number", length = 100)
    private String tinNumber;

    @Column(name = "marital_status", length = 50)
    private String maritalStatus;

    @Column(name = "blood_group", length = 20)
    private String bloodGroup;

    @Column(length = 100)
    private String religion;

    @Column(name = "official_email", length = 150)
    private String officialEmail;

    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Column(name = "mobile_no", length = 30)
    private String mobileNo;

    @Column(name = "alternate_mobile", length = 30)
    private String alternateMobile;

    @Column(name = "address_country", length = 100)
    private String addressCountry;

    @Column(name = "address_state", length = 100)
    private String addressState;

    @Column(name = "address_district", length = 100)
    private String addressDistrict;

    @Column(name = "address_village", length = 150)
    private String addressVillage;

    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_category", length = 30)
    private EmployeeCategory employeeCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 30)
    private EmployeeType employeeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_mode", nullable = false, length = 30)
    private EmploymentMode employmentMode = EmploymentMode.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 30)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "retirement_date")
    private LocalDate retirementDate;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "work_permit_number", length = 100)
    private String workPermitNumber;

    @Column(name = "work_permit_expiry_date")
    private LocalDate workPermitExpiryDate;

    @Column(name = "passport_expiry_date")
    private LocalDate passportExpiryDate;

    @Column(name = "employment_end_date")
    private LocalDate employmentEndDate;

    @Column(name = "exit_reason", columnDefinition = "TEXT")
    private String exitReason;

    @Column(name = "employee_remarks", columnDefinition = "TEXT")
    private String employeeRemarks;

    @Column(name = "login_enabled", nullable = false)
    private Boolean loginEnabled = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "address_county", length = 100)
    private String addressCounty;

    @Column(name = "address_sub_county", length = 100)
    private String addressSubCounty;

    @Column(name = "address_parish", length = 100)
    private String addressParish;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "languages_spoken", columnDefinition = "TEXT")
    private String languagesSpoken;

    @Column(name = "sub_religion", length = 100)
    private String subReligion;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @PrePersist
    @PreUpdate
    private void generateFullName() {
        this.fullName = (firstName != null ? firstName : "") +
                (middleName != null && !middleName.isBlank() ? " " + middleName : "") +
                (lastName != null && !lastName.isBlank() ? " " + lastName : "");
        this.fullName = this.fullName.trim();
    }
}