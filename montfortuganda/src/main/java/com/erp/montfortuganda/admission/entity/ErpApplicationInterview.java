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
@Table(name = "erp_application_interviews")
@EqualsAndHashCode(exclude = "application")
@ToString(exclude = "application")
public class ErpApplicationInterview implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Result {
        PENDING,
        PASSED,
        FAILED,
        WAITLIST
    }

    public enum Status {
        NOT_SCHEDULED,
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long interviewId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_interview_application"
            )
    )
    private ErpApplication application;

    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
            name = "result",
            nullable = false,
            length = 20
    )
    private Result result = Result.PENDING;

    @Size(max = 5000)
    @Column(
            name = "employee_remarks",
            columnDefinition = "TEXT"
    )
    private String employeeRemarks;

    @Size(max = 5000)
    @Column(
            name = "internal_remarks",
            columnDefinition = "TEXT"
    )
    private String internalRemarks;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private Status status = Status.NOT_SCHEDULED;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

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
    private Long version;

    @PrePersist
    private void onCreate() {
        if (result == null) {
            result = Result.PENDING;
        }

        if (status == null) {
            status = Status.NOT_SCHEDULED;
        }

        if (active == null) {
            active = true;
        }

        LocalDateTime now = LocalDateTime.now();

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
