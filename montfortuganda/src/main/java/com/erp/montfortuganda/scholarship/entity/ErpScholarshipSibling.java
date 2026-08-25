package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_scholarship_siblings",
        indexes = {
                @Index(
                        name = "idx_scholarship_sibling_app",
                        columnList = "scholarship_app_id"
                ),
                @Index(
                        name = "idx_scholarship_sibling_status",
                        columnList = "current_status"
                )
        }
)
public class ErpScholarshipSibling implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum CurrentStatus {
        STUDYING,
        WORKING,
        STUDYING_AND_WORKING,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sibling_id")
    private Long siblingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "scholarship_app_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ErpScholarshipApplication scholarshipApplication;

    @Column(
            name = "sibling_name",
            length = 150,
            nullable = false
    )
    private String siblingName;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "current_status",
            length = 30,
            nullable = false
    )
    private CurrentStatus currentStatus;

    @Column(
            name = "class_or_course",
            length = 100
    )
    private String classOrCourse;

    @Column(
            name = "institution",
            length = 200
    )
    private String institution;

    @Column(
            name = "occupation",
            length = 150
    )
    private String occupation;

    @Column(
            name = "annual_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal annualIncome;

    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active = true;

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

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (active == null) {
            active = true;
        }

        if (version == null) {
            version = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (active == null) {
            active = true;
        }
    }
}
