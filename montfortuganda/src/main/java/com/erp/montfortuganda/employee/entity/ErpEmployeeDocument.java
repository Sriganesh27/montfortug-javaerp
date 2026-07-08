package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
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
        name = "erp_employee_documents",
        indexes = {
                @Index(name = "idx_empdoc_employee", columnList = "employee_id"),
                @Index(name = "idx_empdoc_type", columnList = "employee_document_type"),
                @Index(name = "idx_empdoc_status", columnList = "employee_document_verification_status")
        }
)
@EqualsAndHashCode(callSuper = true, exclude = {"employee", "verifiedBy"})
@ToString(callSuper = true, exclude = {"employee", "verifiedBy"})
public class ErpEmployeeDocument extends AuditableEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_document_id")
    private Long employeeDocumentId;

    //==================================================
    // RELATIONSHIPS
    //==================================================

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_empdocument_employee")
    )
    private ErpEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "verified_by",
            foreignKey = @ForeignKey(name = "fk_empdocument_verifiedby")
    )
    private User verifiedBy;

    //==================================================
    // DOCUMENT DETAILS
    //==================================================

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_document_type", nullable = false, length = 50)
    private EmployeeDocumentType employeeDocumentType;

    @NotBlank
    @Size(max = 255)
    @Column(name = "employee_document_name", nullable = false, length = 255)
    private String employeeDocumentName;

    @Size(max = 100)
    @Column(name = "employee_document_number", length = 100)
    private String employeeDocumentNumber;

    @NotBlank
    @Size(max = 500)
    @Column(name = "employee_document_file", nullable = false, length = 500)
    private String employeeDocumentFile;

    @Size(max = 255)
    @Column(name = "employee_document_original_name", length = 255)
    private String employeeDocumentOriginalName;

    @Size(max = 100)
    @Column(name = "employee_document_content_type", length = 100)
    private String employeeDocumentContentType;

    @Column(name = "employee_document_file_size")
    private Long employeeDocumentFileSize;

    @Size(max = 255)
    @Column(name = "employee_document_issued_by", length = 255)
    private String employeeDocumentIssuedBy;

    //==================================================
    // DATES
    //==================================================

    @Column(name = "employee_document_issue_date")
    private LocalDate employeeDocumentIssueDate;

    @Column(name = "employee_document_expiry_date")
    private LocalDate employeeDocumentExpiryDate;

    //==================================================
    // VERIFICATION
    //==================================================

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_document_verification_status", nullable = false, length = 30)
    private VerificationStatus employeeDocumentVerificationStatus =
            VerificationStatus.PENDING;

    @Column(name = "employee_document_verified_at")
    private LocalDateTime employeeDocumentVerifiedAt;

    @Column(name = "employee_document_rejection_reason", columnDefinition = "TEXT")
    private String employeeDocumentRejectionReason;

    //==================================================
    // OTHER DETAILS
    //==================================================

    @Column(name = "employee_document_mandatory")
    private Boolean employeeDocumentMandatory = false;

    @Column(name = "employee_document_remarks", columnDefinition = "TEXT")
    private String employeeDocumentRemarks;

    @NotNull
    @Column(name = "employee_document_active", nullable = false)
    private Boolean employeeDocumentActive = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    public void prePersist() {

        if (employeeDocumentVerificationStatus == null)
            employeeDocumentVerificationStatus = VerificationStatus.PENDING;

        if (employeeDocumentMandatory == null)
            employeeDocumentMandatory = false;

        if (employeeDocumentActive == null)
            employeeDocumentActive = true;

        if (version == null)
            version = 0L;
    }
}