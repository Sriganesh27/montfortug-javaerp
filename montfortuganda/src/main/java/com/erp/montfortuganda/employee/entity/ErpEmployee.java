// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployee.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.enums.Gender;
import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "erp_employees",
        indexes = {
                @Index(name = "idx_emp_branch", columnList = "branch_id"),
                @Index(name = "idx_emp_department", columnList = "department_id"),
                @Index(name = "idx_emp_designation", columnList = "designation_id"),
                @Index(name = "idx_emp_status", columnList = "employment_status"),
                @Index(name = "idx_emp_category", columnList = "employee_category")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_no",
                        columnNames = "employee_no"
                ),
                @UniqueConstraint(
                        name = "uk_employee_user",
                        columnNames = "user_id"
                )
        }
)
public class ErpEmployee extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ErpEmployee reportingManager;

    @Column(name = "employee_no", nullable = false, unique = true, length = 50)
    private String employeeNo;

    @Column(name = "title", length = 20)
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
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_photo", length = 500)
    private String profilePhoto;

    @Column(name = "signature_file", length = 500)
    private String signatureFile;

    @Column(name = "nationality", length = 100)
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

    @Column(name = "religion", length = 100)
    private String religion;

    @Column(name = "sub_religion", length = 100)
    private String subReligion;

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

    @Column(name = "address_county", length = 100)
    private String addressCounty;

    @Column(name = "address_sub_county", length = 100)
    private String addressSubCounty;

    @Column(name = "address_parish", length = 100)
    private String addressParish;

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

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "languages_spoken", columnDefinition = "TEXT")
    private String languagesSpoken;

    @Column(name = "employee_remarks", columnDefinition = "TEXT")
    private String employeeRemarks;

    @Column(name = "login_enabled", nullable = false)
    private Boolean loginEnabled = false;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    @PreUpdate
    private void generateFullName() {
        StringBuilder name = new StringBuilder();

        if (firstName != null && !firstName.isBlank()) {
            name.append(firstName.trim());
        }

        if (middleName != null && !middleName.isBlank()) {
            if (!name.isEmpty()) {
                name.append(" ");
            }
            name.append(middleName.trim());
        }

        if (lastName != null && !lastName.isBlank()) {
            if (!name.isEmpty()) {
                name.append(" ");
            }
            name.append(lastName.trim());
        }

        this.fullName = name.toString();
    }
}