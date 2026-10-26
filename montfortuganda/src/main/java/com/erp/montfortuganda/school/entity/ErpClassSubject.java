package com.erp.montfortuganda.school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/** Academic-year-specific subject requirement for a level and a class range within that level. */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_class_subjects", indexes = {
        @Index(name = "idx_class_subject_branch_year", columnList = "branch_id, academic_year_id, active"),
        @Index(name = "idx_class_subject_subject", columnList = "subject_id"),
        @Index(name = "idx_class_subject_start", columnList = "start_class_id"),
        @Index(name = "idx_class_subject_end", columnList = "end_class_id")
})
@EqualsAndHashCode(exclude = {"branch", "academicYear", "level", "subject", "startClass", "endClass"})
@ToString(exclude = {"branch", "academicYear", "level", "subject", "startClass", "endClass"})
public class ErpClassSubject {

    public enum Requirement { COMPULSORY, OPTIONAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_subject_id")
    private Long classSubjectId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private ErpAcademicYear academicYear;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private ErpSubject subject;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "start_class_id", nullable = false)
    private SchoolClass startClass;

    /** Null means this rule applies only to startClass. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_class_id")
    private SchoolClass endClass;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_requirement", nullable = false, length = 20)
    private Requirement subjectRequirement;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (active == null) active = true;
        if (version == null) version = 0L;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
