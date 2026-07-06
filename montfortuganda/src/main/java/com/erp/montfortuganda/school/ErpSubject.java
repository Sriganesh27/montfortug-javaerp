package com.erp.montfortuganda.school;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_subjects",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subject_code",
                        columnNames = {"subject_code"}
                )
        }
)
public class ErpSubject implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Status { ACTIVE, INACTIVE }
    public enum SubjectType { CORE, ELECTIVE, OPTIONAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Long subjectId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "subject_code", nullable = false, unique = true, length = 20)
    private String subjectCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;

    @Size(max = 50)
    @Column(name = "subject_short_name", length = 50)
    private String subjectShortName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 30)
    private SubjectType subjectType = SubjectType.CORE;

    @NotNull
    @Column(name = "is_practical", nullable = false)
    private Boolean isPractical = false;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.ACTIVE;

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
    private void onCreate() {
        if (subjectType == null) subjectType = SubjectType.CORE;
        if (isPractical == null) isPractical = false;
        if (displayOrder == null) displayOrder = 1;
        if (active == null) active = true;
        if (status == null) status = Status.ACTIVE;

        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}