package com.erp.montfortuganda.admission.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
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

    public enum Recommendation { RECOMMENDED, NOT_RECOMMENDED, WAITLIST }
    public enum Status { PENDING, IN_PROGRESS, SUBMITTED, REVIEWED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long interviewId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_interview_app")
    )
    private ErpApplication application;

    // TODO: Change to @ManyToOne ErpStaff when Staff module is built
    @NotNull
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Column(name = "test_score", precision = 5, scale = 2)
    private BigDecimal testScore;

    // Cap at 5000 to prevent payload abuse while allowing substantial feedback
    @Size(max = 5000)
    @Column(name = "teacher_remarks", columnDefinition = "TEXT")
    private String teacherRemarks;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false, length = 30)
    private Recommendation recommendation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status = Status.PENDING;

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