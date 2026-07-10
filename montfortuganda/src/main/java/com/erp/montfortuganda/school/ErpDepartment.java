package com.erp.montfortuganda.school;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import com.erp.montfortuganda.school.enums.DepartmentType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_departments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_branch_dept_code",
                        columnNames = {"branch_id", "department_code"}
                )
        }
)
@EqualsAndHashCode(exclude = {"branch"})
@ToString(exclude = {"branch"})
public class ErpDepartment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Status { ACTIVE, INACTIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "branch_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_department_branch")
    )
    private Branch branch;

    @NotBlank
    @Size(max = 20)
    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Enumerated(EnumType.STRING)
    private DepartmentType departmentType;

    private Integer displayOrder = 99;

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
        if (departmentType == null) departmentType = DepartmentType.ACADEMIC;
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