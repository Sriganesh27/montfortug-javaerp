package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.*;
import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.ErpDepartment;
import com.erp.montfortuganda.school.ErpDesignation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_employees",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_no", columnNames = "employee_no"),
                @UniqueConstraint(name = "uk_employee_user", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_emp_branch", columnList = "branch_id"),
                @Index(name = "idx_emp_department", columnList = "department_id"),
                @Index(name = "idx_emp_designation", columnList = "designation_id"),
                @Index(name = "idx_emp_status", columnList = "employment_status"),
                @Index(name = "idx_emp_category", columnList = "employee_category")
        }
)
@EqualsAndHashCode(callSuper = true, exclude = {"user", "branch", "department", "designation", "reportingManager", "subordinates"})
@ToString(callSuper = true, exclude = {"user", "branch", "department", "designation", "reportingManager", "subordinates"})
public class ErpEmployee extends AuditableEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    // --- RELATIONS ---

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_employee_user"))
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_employee_branch"))
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_employee_department"))
    private ErpDepartment department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id", foreignKey = @ForeignKey(name = "fk_employee_designation"))
    private ErpDesignation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id", foreignKey = @ForeignKey(name = "fk_employee_reporting_manager"))
    private ErpEmployee reportingManager;

    @OneToMany(mappedBy = "reportingManager", fetch = FetchType.LAZY)
    private List<ErpEmployee> subordinates = new ArrayList<>();

    // --- CORE DETAILS ---

    @NotBlank
    @Size(max = 50)
    @Column(name = "employee_no", nullable = false, updatable = false, length = 50)
    private String employeeNo;

    @Size(max = 20)
    @Column(name = "title", length = 20)
    private String title;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100)
    @Column(name = "middle_name", length = 100)
    private String middleName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 255)
    @Column(name = "full_name", length = 255)
    private String fullName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private EmployeeGender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 500)
    @Column(name = "profile_photo", length = 500)
    private String profilePhoto;

    @Size(max = 500)
    @Column(name = "signature_file", length = 500)
    private String signatureFile;

    // --- IDENTITY & CONTACT ---

    @Size(max = 100)
    @Column(name = "nationality", length = 100)
    private String nationality;

    @Size(max = 100)
    @Column(name = "national_id", length = 100)
    private String nationalId;

    @Size(max = 100)
    @Column(name = "passport_no", length = 100)
    private String passportNo;

    @Column(name = "passport_expiry_date")
    private LocalDate passportExpiryDate;

    @Size(max = 100)
    @Column(name = "tin_number", length = 100)
    private String tinNumber;

    @Size(max = 100)
    @Column(name = "work_permit_number", length = 100)
    private String workPermitNumber;

    @Column(name = "work_permit_expiry_date")
    private LocalDate workPermitExpiryDate;

    @Size(max = 50)
    @Column(name = "marital_status", length = 50)
    private String maritalStatus;

    @Size(max = 20)
    @Column(name = "blood_group", length = 20)
    private String bloodGroup;

    @Size(max = 100)
    @Column(name = "religion", length = 100)
    private String religion;

    @Size(max = 150)
    @Column(name = "official_email", length = 150)
    private String officialEmail;

    @Size(max = 150)
    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Size(max = 30)
    @Column(name = "mobile_no", length = 30)
    private String mobileNo;

    @Size(max = 30)
    @Column(name = "alternate_mobile", length = 30)
    private String alternateMobile;

    // --- ADDRESS ---

    @Size(max = 100)
    @Column(name = "address_country", length = 100)
    private String addressCountry;

    @Size(max = 100)
    @Column(name = "address_state", length = 100)
    private String addressState;

    @Size(max = 100)
    @Column(name = "address_district", length = 100)
    private String addressDistrict;

    @Size(max = 150)
    @Column(name = "address_village", length = 150)
    private String addressVillage;

    @Size(max = 255)
    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Size(max = 30)
    @Column(name = "postal_code", length = 30)
    private String postalCode;

    // --- EMPLOYMENT DETAILS ---

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_category", nullable = false, length = 50)
    private EmployeeCategory employeeCategory;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 30)
    private EmployeeType employeeType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_mode", nullable = false, length = 30)
    private EmploymentMode employmentMode = EmploymentMode.FULL_TIME;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 30)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @NotNull
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "employment_end_date")
    private LocalDate employmentEndDate;

    @Column(name = "retirement_date")
    private LocalDate retirementDate;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "exit_reason", columnDefinition = "TEXT")
    private String exitReason;

    // --- STATUS, REMARKS & AUDIT ---

    @Column(name = "employee_remarks", columnDefinition = "TEXT")
    private String employeeRemarks;

    @NotNull
    @Column(name = "login_enabled", nullable = false)
    private Boolean loginEnabled = false;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    @PreUpdate
    public void updateFullNameAndDefaults() {
        if (this.employmentStatus == null) this.employmentStatus = EmploymentStatus.ACTIVE;
        if (this.employmentMode == null) this.employmentMode = EmploymentMode.FULL_TIME;
        if (this.active == null) this.active = true;
        if (this.version == null) this.version = 0L;
        if (this.loginEnabled == null) this.loginEnabled = false;

        // Dynamically calculate full name to handle marriage/name changes
        this.fullName = String.join(" ",
                this.firstName,
                this.middleName == null ? "" : this.middleName,
                this.lastName
        ).replaceAll("\\s+", " ").trim();
    }
}