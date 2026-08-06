package com.erp.montfortuganda.admission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(name = "erp_application_documents")
@EqualsAndHashCode(exclude = {
        "application",
        "documentRequest",
        "replacementDocument"
})
@ToString(exclude = {
        "application",
        "documentRequest",
        "replacementDocument"
})
public class ErpApplicationDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum DocumentType {
        PHOTO,
        BIRTH_CERTIFICATE,
        REPORT_CARD,
        PREVIOUS_MARKS,
        TRANSFER_LETTER,
        PASSPORT,
        NATIONAL_ID,
        IMMUNIZATION_CARD,
        MEDICAL_REPORT,
        RECOMMENDATION_LETTER,
        OTHER
    }

    public enum SubmissionSource {
        PUBLIC_PORTAL,
        BRANCH_ADMIN,
        SCHOOL_ASSISTED,
        SCHOLARSHIP_PORTAL,
        SYSTEM
    }

    public enum VerificationStatus {
        PENDING,
        VERIFIED,
        REJECTED,
        REUPLOAD_REQUIRED,
        SUPERSEDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_application_document_application"
            )
    )
    private ErpApplication application;

    /**
     * Set only when this file was uploaded in response to a specific
     * additional-document or re-upload request.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "document_request_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_app_doc_request")
    )
    private ErpApplicationDocumentRequest documentRequest;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "submission_source", nullable = false, length = 30)
    private SubmissionSource submissionSource = SubmissionSource.PUBLIC_PORTAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    /**
     * Only the latest active version of a logical document remains current.
     */
    @NotNull
    @Column(name = "is_current", nullable = false)
    private Boolean current = true;

    @NotBlank
    @Size(max = 255)
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @NotBlank
    @Size(max = 500)
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PositiveOrZero
    @Column(name = "file_size")
    private Long fileSize;

    @Size(max = 100)
    @Column(name = "content_type", length = 100)
    private String contentType;

    @Size(max = 64)
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    /**
     * Existing uploader audit field. It remains BIGINT because the current
     * database column and public-upload flow already use that type.
     */
    @Column(name = "uploaded_by")
    private Long uploadedBy;

    /**
     * References erp_users.id, whose database type is INT.
     */
    @Column(name = "verified_by_user_id")
    private Integer verifiedByUserId;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /**
     * References erp_users.id, whose database type is INT.
     */
    @Column(name = "rejected_by_user_id")
    private Integer rejectedByUserId;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Size(max = 1000)
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Size(max = 1000)
    @Column(name = "public_remarks", length = 1000)
    private String publicRemarks;

    @Size(max = 1000)
    @Column(name = "internal_remarks", length = 1000)
    private String internalRemarks;

    @Column(name = "reupload_requested_at")
    private LocalDateTime reuploadRequestedAt;

    @Column(name = "reupload_deadline")
    private LocalDateTime reuploadDeadline;

    /**
     * Points from an older document to the newer file that replaced it.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "replacement_document_id",
            foreignKey = @ForeignKey(name = "fk_app_doc_replacement")
    )
    private ErpApplicationDocument replacementDocument;

    @Column(name = "superseded_at")
    private LocalDateTime supersededAt;

    /**
     * References erp_users.id, whose database type is INT.
     */
    @Column(name = "superseded_by_user_id")
    private Integer supersededByUserId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (submissionSource == null) {
            submissionSource = SubmissionSource.PUBLIC_PORTAL;
        }
        if (verificationStatus == null) {
            verificationStatus = VerificationStatus.PENDING;
        }
        if (current == null) {
            current = true;
        }
        if (active == null) {
            active = true;
        }
        if (uploadedAt == null) {
            uploadedAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
