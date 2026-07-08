package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.VerificationStatus;
import com.erp.montfortuganda.model.AuditableEntity;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_employee_experience",
        indexes = {
                @Index(name = "idx_empexp_employee", columnList = "employee_id"),
                @Index(name = "idx_empexp_status", columnList = "employee_experience_verification_status")
        }
)
@EqualsAndHashCode(callSuper = true, exclude = {"employee", "verifiedBy"})
@ToString(callSuper = true, exclude = {"employee", "verifiedBy"})
public class ErpEmployeeExperience extends AuditableEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_experience_id")
    private Long employeeExperienceId;

    //==================================================
    // RELATIONSHIPS
    //==================================================

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_empexperience_employee")
    )
    private ErpEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "verified_by",
            foreignKey = @ForeignKey(name = "fk_empexperience_verifiedby")
    )
    private User verifiedBy;

    //==================================================
    // COMPANY DETAILS
    //==================================================

    @NotBlank
    @Size(max = 255)
    @Column(name = "employee_experience_company_name", nullable = false, length = 255)
    private String employeeExperienceCompanyName;

    @Size(max = 255)
    @Column(name = "employee_experience_company_address", length = 255)
    private String employeeExperienceCompanyAddress;

    @Size(max = 100)
    @Column(name = "employee_experience_company_country", length = 100)
    private String employeeExperienceCompanyCountry;

    @Size(max = 100)
    @Column(name = "employee_experience_company_district", length = 100)
    private String employeeExperienceCompanyDistrict;

    @Size(max = 100)
    @Column(name = "employee_experience_company_sub_county", length = 100)
    private String employeeExperienceCompanySubCounty;

    @Size(max = 100)
    @Column(name = "employee_experience_company_parish", length = 100)
    private String employeeExperienceCompanyParish;

    @Size(max = 150)
    @Column(name = "employee_experience_company_email", length = 150)
    private String employeeExperienceCompanyEmail;

    @Size(max = 30)
    @Column(name = "employee_experience_company_phone", length = 30)
    private String employeeExperienceCompanyPhone;

    //==================================================
    // JOB ROLE & DATES
    //==================================================

    @Size(max = 150)
    @Column(name = "employee_experience_designation", length = 150)
    private String employeeExperienceDesignation;

    @Size(max = 150)
    @Column(name = "employee_experience_department", length = 150)
    private String employeeExperienceDepartment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_experience_employment_type", nullable = false, length = 50)
    private EmployeeType employeeExperienceEmploymentType;

    @NotNull
    @Column(name = "employee_experience_start_date", nullable = false)
    private LocalDate employeeExperienceStartDate;

    @Column(name = "employee_experience_end_date")
    private LocalDate employeeExperienceEndDate;

    @NotNull
    @Column(name = "employee_experience_current_job", nullable = false)
    private Boolean employeeExperienceCurrentJob = false;

    //==================================================
    // COMPENSATION & DUTIES
    //==================================================

    // Note: All salaries implicitly assumed as UGX for this ERP.
    @Column(name = "employee_experience_salary", precision = 15, scale = 2)
    private BigDecimal employeeExperienceSalary;

    @Column(name = "employee_experience_responsibilities", columnDefinition = "TEXT")
    private String employeeExperienceResponsibilities;

    @Column(name = "employee_experience_achievements", columnDefinition = "TEXT")
    private String employeeExperienceAchievements;

    @Size(max = 255)
    @Column(name = "employee_experience_reason_for_leaving", length = 255)
    private String employeeExperienceReasonForLeaving;

    //==================================================
    // REFERENCES & HR VERIFICATION
    //==================================================

    @Size(max = 255)
    @Column(name = "employee_experience_supervisor_name", length = 255)
    private String employeeExperienceSupervisorName;

    @Size(max = 100)
    @Column(name = "employee_experience_supervisor_contact", length = 100)
    private String employeeExperienceSupervisorContact;

    @Size(max = 150)
    @Column(name = "employee_experience_supervisor_email", length = 150)
    private String employeeExperienceSupervisorEmail;

    @Size(max = 255)
    @Column(name = "employee_experience_hr_contact_name", length = 255)
    private String employeeExperienceHrContactName;

    @Size(max = 150)
    @Column(name = "employee_experience_hr_contact_email", length = 150)
    private String employeeExperienceHrContactEmail;

    //==================================================
    // FILES & METADATA
    //==================================================

    @Size(max = 500)
    @Column(name = "employee_experience_experience_certificate_file", length = 500)
    private String employeeExperienceExperienceCertificateFile;

    @Size(max = 255)
    @Column(name = "employee_experience_certificate_original_name", length = 255)
    private String employeeExperienceCertificateOriginalName;

    @Size(max = 100)
    @Column(name = "employee_experience_certificate_content_type", length = 100)
    private String employeeExperienceCertificateContentType;

    @Column(name = "employee_experience_certificate_file_size")
    private Long employeeExperienceCertificateFileSize;

    @Size(max = 500)
    @Column(name = "employee_experience_relieving_letter_file", length = 500)
    private String employeeExperienceRelievingLetterFile;

    @Size(max = 255)
    @Column(name = "employee_experience_relieving_letter_original_name", length = 255)
    private String employeeExperienceRelievingLetterOriginalName;

    @Size(max = 100)
    @Column(name = "employee_experience_relieving_letter_content_type", length = 100)
    private String employeeExperienceRelievingLetterContentType;

    @Column(name = "employee_experience_relieving_letter_file_size")
    private Long employeeExperienceRelievingLetterFileSize;

    //==================================================
    // VERIFICATION
    //==================================================

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_experience_verification_status", nullable = false, length = 30)
    private VerificationStatus employeeExperienceVerificationStatus = VerificationStatus.PENDING;

    @Column(name = "employee_experience_verified_at")
    private LocalDateTime employeeExperienceVerifiedAt;

    @Column(name = "employee_experience_rejection_reason", columnDefinition = "TEXT")
    private String employeeExperienceRejectionReason;

    @Column(name = "employee_experience_remarks", columnDefinition = "TEXT")
    private String employeeExperienceRemarks;

    //==================================================
    // STATUS
    //==================================================

    @NotNull
    @Column(name = "employee_experience_active", nullable = false)
    private Boolean employeeExperienceActive = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    public void prePersist() {
        if (employeeExperienceCurrentJob == null)
            employeeExperienceCurrentJob = false;

        if (employeeExperienceVerificationStatus == null)
            employeeExperienceVerificationStatus = VerificationStatus.PENDING;

        if (employeeExperienceActive == null)
            employeeExperienceActive = true;

        if (version == null)
            version = 0L;
    }
}