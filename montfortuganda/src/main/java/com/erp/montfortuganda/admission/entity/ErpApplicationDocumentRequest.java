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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "erp_application_document_requests")
@EqualsAndHashCode(exclude = "application")
@ToString(exclude = "application")
public class ErpApplicationDocumentRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum RequestStatus {
        PENDING,
        UPLOADED,
        COMPLETED,
        CANCELLED,
        EXPIRED
    }

    public enum EmailStatus {
        NOT_REQUIRED,
        PENDING,
        SENT,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_doc_request_application")
    )
    private ErpApplication application;

    /**
     * Stored as text so the school can request both standard document types
     * and future/custom document types without requiring a database enum change.
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "requested_document_type", nullable = false, length = 50)
    private String requestedDocumentType;

    @NotBlank
    @Size(max = 150)
    @Column(name = "requested_document_name", nullable = false, length = 150)
    private String requestedDocumentName;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "request_reason", nullable = false, length = 1000)
    private String requestReason;

    @Size(max = 1000)
    @Column(name = "public_remarks", length = 1000)
    private String publicRemarks;

    @Size(max = 1000)
    @Column(name = "internal_remarks", length = 1000)
    private String internalRemarks;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 30)
    private RequestStatus requestStatus = RequestStatus.PENDING;

    /**
     * References erp_users.id, whose database type is INT.
     */
    @NotNull
    @Column(name = "requested_by_user_id", nullable = false)
    private Integer requestedByUserId;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "upload_deadline")
    private LocalDateTime uploadDeadline;

    /**
     * SHA-256 hash of the raw upload token. The raw token must never be stored.
     */
    @Size(max = 64)
    @Column(name = "upload_token_hash", length = 64, unique = true)
    private String uploadTokenHash;

    @Column(name = "upload_token_expires_at")
    private LocalDateTime uploadTokenExpiresAt;

    @Column(name = "token_used_at")
    private LocalDateTime tokenUsedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * References erp_users.id, whose database type is INT.
     */
    @Column(name = "completed_by_user_id")
    private Integer completedByUserId;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * References erp_users.id, whose database type is INT.
     */
    @Column(name = "cancelled_by_user_id")
    private Integer cancelledByUserId;

    @Size(max = 1000)
    @Column(name = "cancellation_reason", length = 1000)
    private String cancellationReason;

    @NotNull
    @Column(name = "email_required", nullable = false)
    private Boolean emailRequired = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "email_status", nullable = false, length = 30)
    private EmailStatus emailStatus = EmailStatus.PENDING;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (requestStatus == null) {
            requestStatus = RequestStatus.PENDING;
        }
        if (requestedAt == null) {
            requestedAt = now;
        }
        if (emailRequired == null) {
            emailRequired = true;
        }
        if (emailStatus == null) {
            emailStatus = emailRequired
                    ? EmailStatus.PENDING
                    : EmailStatus.NOT_REQUIRED;
        }
        if (active == null) {
            active = true;
        }
        if (createdAt == null) {
            createdAt = now;
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
