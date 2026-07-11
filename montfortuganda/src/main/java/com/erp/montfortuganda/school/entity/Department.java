package com.erp.montfortuganda.school.entity;

import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.Branch;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "erp_departments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_branch_dept_code", columnNames = {"branch_id", "department_code"})
        }
)
public class Department extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "is_academic", nullable = false)
    private Boolean isAcademic = true;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}