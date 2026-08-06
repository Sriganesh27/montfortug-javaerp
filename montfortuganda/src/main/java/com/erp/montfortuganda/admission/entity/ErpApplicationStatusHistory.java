package com.erp.montfortuganda.admission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "erp_application_status_history")
@EqualsAndHashCode(exclude = "application")
@ToString(exclude = "application")
public class ErpApplicationStatusHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_STAGE = "APPLICATION";
    public static final String DEFAULT_TRANSITION_SOURCE = "ERP";
    public static final String EMAIL_NOT_REQUIRED = "NOT_REQUIRED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_application_status_history_application")
    )
    private ErpApplication application;

    /**
     * Workflow area that produced this history entry, for example:
     * APPLICATION, APPLICATION_WORKFLOW, DOCUMENT_VERIFICATION,
     * SCHOOL_VISIT, ENTRANCE_TEST, FEES, PAYMENT or SCHOLARSHIP.
     */
    @NotNull
    @Column(name = "stage", nullable = false, length = 50)
    private String stage = DEFAULT_STAGE;

    /**
     * Stored as text because one history table records transitions from
     * multiple workflow areas, not only ErpApplication.ApplicationStatus.
     */
    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @NotNull
    @Column(name = "new_status", nullable = false, length = 50)
    private String newStatus;

    @Column(name = "changed_by")
    private Long changedBy;

    /**
     * Legacy remarks column retained for compatibility with existing code.
     * New workflow code should prefer publicRemarks and internalRemarks.
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "public_remarks", columnDefinition = "TEXT")
    private String publicRemarks;

    @Column(name = "internal_remarks", columnDefinition = "TEXT")
    private String internalRemarks;

    @NotNull
    @Column(name = "transition_source", nullable = false, length = 30)
    private String transitionSource = DEFAULT_TRANSITION_SOURCE;

    @NotNull
    @Column(name = "email_required", nullable = false)
    private Boolean emailRequired = false;

    @NotNull
    @Column(name = "email_status", nullable = false, length = 30)
    private String emailStatus = EMAIL_NOT_REQUIRED;

    @Column(name = "email_type", length = 50)
    private String emailType;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    public void setOldStatus(String status) {
        this.oldStatus = status;
    }

    public void setNewStatus(String status) {
        this.newStatus = status;
    }

    /**
     * Compatibility overload for existing and future enum-backed workflow statuses.
     */
    public void setOldStatus(Enum<?> status) {
        this.oldStatus = status == null ? null : status.name();
    }

    /**
     * Compatibility overload for existing and future enum-backed workflow statuses.
     */
    public void setNewStatus(Enum<?> status) {
        this.newStatus = status == null ? null : status.name();
    }

    @PrePersist
    private void onCreate() {
        if (stage == null || stage.isBlank()) {
            stage = DEFAULT_STAGE;
        }
        if (transitionSource == null || transitionSource.isBlank()) {
            transitionSource = DEFAULT_TRANSITION_SOURCE;
        }
        if (emailRequired == null) {
            emailRequired = false;
        }
        if (emailStatus == null || emailStatus.isBlank()) {
            emailStatus = emailRequired ? "PENDING" : EMAIL_NOT_REQUIRED;
        }
        if (active == null) {
            active = true;
        }
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}
