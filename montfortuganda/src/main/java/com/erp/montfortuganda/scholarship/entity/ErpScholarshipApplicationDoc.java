package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(name = "erp_scholarship_application_docs")
@EqualsAndHashCode(exclude = "scholarshipApplication")
@ToString(exclude = "scholarshipApplication")
public class ErpScholarshipApplicationDoc {

    public enum DocumentType {
        STUDENT_BIRTH_CERTIFICATE,
        STUDENT_PHOTO,
        ACADEMIC_REPORT,
        PARENT_NATIONAL_ID,
        GUARDIANSHIP_PROOF,
        INCOME_PROOF,
        EMPLOYER_LETTER,
        DEATH_CERTIFICATE,
        MEDICAL_REPORT,
        DISABILITY_SUPPORT,
        HARDSHIP_SUPPORT,

        /*
         * Keep the existing document types for backward compatibility.
         */
        INCOME_STATEMENT,
        SPORTS_CERTIFICATE,
        RECOMMENDATION,
        OTHER
    }

    public enum VerificationStatus {
        PENDING,
        VERIFIED,
        REJECTED,
        REUPLOAD_REQUIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "scholarship_app_id",
            nullable = false
    )
    private ErpScholarshipApplication scholarshipApplication;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "document_type",
            length = 50,
            nullable = false
    )
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "verification_status",
            length = 20,
            nullable = false
    )
    private VerificationStatus verificationStatus =
            VerificationStatus.PENDING;

    @Column(
            name = "original_file_name",
            length = 255,
            nullable = false
    )
    private String originalFileName;

    @Column(
            name = "stored_file_name",
            length = 255,
            nullable = false
    )
    private String storedFileName;

    @Column(
            name = "file_path",
            length = 500,
            nullable = false
    )
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(
            name = "content_type",
            length = 100
    )
    private String contentType;

    @Column(
            name = "file_hash",
            length = 64
    )
    private String fileHash;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(
            name = "uploaded_at",
            nullable = false
    )
    private LocalDateTime uploadedAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active = true;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version = 0L;

    @PrePersist
    private void onCreate() {
        if (verificationStatus == null) {
            verificationStatus =
                    VerificationStatus.PENDING;
        }

        if (active == null) {
            active = true;
        }

        LocalDateTime now =
                LocalDateTime.now();

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
