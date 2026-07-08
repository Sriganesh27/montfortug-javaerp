package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.EmployeeQualificationLevel;
import com.erp.montfortuganda.employee.enums.EmployeeQualificationStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_employee_qualifications",
        indexes = {
                @Index(name = "idx_empqual_employee", columnList = "employee_id"),
                @Index(name = "idx_empqual_level", columnList = "employee_qualification_level"),
                @Index(name = "idx_empqual_status", columnList = "employee_qualification_verification_status")
        }
)
@EqualsAndHashCode(callSuper = true, exclude = {"employee", "verifiedBy"})
@ToString(callSuper = true, exclude = {"employee", "verifiedBy"})
public class ErpEmployeeQualification extends AuditableEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_qualification_id")
    private Long employeeQualificationId;

    //==========================
    // RELATIONSHIPS
    //==========================

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_empqualification_employee")
    )
    private ErpEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "verified_by",
            foreignKey = @ForeignKey(name = "fk_empqualification_verifiedby")
    )
    private User verifiedBy;

    //==========================
    // QUALIFICATION DETAILS
    //==========================

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_qualification_level", nullable = false, length = 50)
    private EmployeeQualificationLevel employeeQualificationLevel;

    @NotBlank
    @Size(max = 255)
    @Column(name = "employee_qualification_name", nullable = false, length = 255)
    private String employeeQualificationName;

    @Size(max = 255)
    @Column(name = "employee_qualification_specialization", length = 255)
    private String employeeQualificationSpecialization;

    @NotBlank
    @Size(max = 255)
    @Column(name = "employee_qualification_institution_name", nullable = false, length = 255)
    private String employeeQualificationInstitutionName;

    @Size(max = 255)
    @Column(name = "employee_qualification_board_university", length = 255)
    private String employeeQualificationBoardUniversity;

    @Size(max = 100)
    @Column(name = "employee_qualification_country", length = 100)
    private String employeeQualificationCountry;

    @Size(max = 255)
    @Column(name = "employee_qualification_equivalent", length = 255)
    private String employeeQualificationEquivalent;

    //==========================
    // DATES & PERFORMANCE
    //==========================

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_qualification_status", length = 30)
    private EmployeeQualificationStatus employeeQualificationStatus;

    @Column(name = "employee_qualification_start_year")
    private Integer employeeQualificationStartYear;

    @Column(name = "employee_qualification_completion_year")
    private Integer employeeQualificationCompletionYear;

    @Column(name = "employee_qualification_award_date")
    private LocalDate employeeQualificationAwardDate;

    @Column(name = "employee_qualification_duration_months")
    private Integer employeeQualificationDurationMonths;

    @Size(max = 50)
    @Column(name = "employee_qualification_grade", length = 50)
    private String employeeQualificationGrade;

    @Column(name = "employee_qualification_percentage")
    private Double employeeQualificationPercentage;

    @Column(name = "employee_qualification_cgpa")
    private Double employeeQualificationCgpa;

    //==========================
    // CERTIFICATE & FILES
    //==========================

    @Size(max = 100)
    @Column(name = "employee_qualification_certificate_number", length = 100)
    private String employeeQualificationCertificateNumber;

    @Size(max = 100)
    @Column(name = "employee_qualification_registration_number", length = 100)
    private String employeeQualificationRegistrationNumber;

    @Size(max = 500)
    @Column(name = "employee_qualification_document_file", length = 500)
    private String employeeQualificationDocumentFile;

    @Size(max = 255)
    @Column(name = "employee_qualification_document_original_name", length = 255)
    private String employeeQualificationDocumentOriginalName;

    @Size(max = 100)
    @Column(name = "employee_qualification_document_content_type", length = 100)
    private String employeeQualificationDocumentContentType;

    @Column(name = "employee_qualification_document_file_size")
    private Long employeeQualificationDocumentFileSize;

    //==========================
    // VERIFICATION
    //==========================

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_qualification_verification_status", nullable = false, length = 30)
    private VerificationStatus employeeQualificationVerificationStatus =
            VerificationStatus.PENDING;

    @Column(name = "employee_qualification_verified_at")
    private LocalDateTime employeeQualificationVerifiedAt;

    @Column(name = "employee_qualification_rejection_reason", columnDefinition = "TEXT")
    private String employeeQualificationRejectionReason;

    @Column(name = "employee_qualification_remarks", columnDefinition = "TEXT")
    private String employeeQualificationRemarks;

    //==========================
    // STATUS
    //==========================

    @NotNull
    @Column(name = "employee_qualification_active", nullable = false)
    private Boolean employeeQualificationActive = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    public void prePersist() {

        if (employeeQualificationVerificationStatus == null)
            employeeQualificationVerificationStatus = VerificationStatus.PENDING;

        if (employeeQualificationActive == null)
            employeeQualificationActive = true;

        if (version == null)
            version = 0L;
    }

}