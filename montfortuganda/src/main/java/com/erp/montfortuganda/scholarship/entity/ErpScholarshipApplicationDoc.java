package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.*;
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
@Table(name = "erp_scholarship_application_docs")
@EqualsAndHashCode(exclude = "scholarshipApplication")
@ToString(exclude = "scholarshipApplication")
public class ErpScholarshipApplicationDoc implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum DocumentType { INCOME_STATEMENT, SPORTS_CERTIFICATE, RECOMMENDATION, OTHER }
    public enum VerificationStatus { PENDING, VERIFIED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "scholarship_app_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_scholarship_doc_app")
    )
    private ErpScholarshipApplication scholarshipApplication;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @NotBlank
    @Size(max = 255)
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @NotBlank
    @Size(max = 500)
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @PositiveOrZero
    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        if (active == null) active = true;
        LocalDateTime now = LocalDateTime.now();
        if (uploadedAt == null) uploadedAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}