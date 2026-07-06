package com.erp.montfortuganda.school;

import jakarta.persistence.*;
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
@Table(
        name = "erp_sections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_branch_year_class_section",
                        columnNames = {"branch_id", "academic_year_id", "class_id", "section_code"}
                )
        }
)
@EqualsAndHashCode(exclude = {"branch", "academicYear", "schoolClass"})
@ToString(exclude = {"branch", "academicYear", "schoolClass"})
public class ErpSection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Status { ACTIVE, INACTIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_section_branch"))
    private Branch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false, foreignKey = @ForeignKey(name = "fk_section_year"))
    private ErpAcademicYear academicYear;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_section_class"))
    private SchoolClass schoolClass;

    @NotBlank
    @Size(max = 20)
    @Column(name = "section_code", nullable = false, length = 20)
    private String sectionCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "section_name", nullable = false, length = 100)
    private String sectionName;

    @NotNull
    @Column(name = "capacity", nullable = false)
    private Integer capacity = 40;

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
        if (active == null) active = true;
        if (status == null) status = Status.ACTIVE;
        if (capacity == null) capacity = 40;

        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}