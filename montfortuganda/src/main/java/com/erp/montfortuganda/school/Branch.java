package com.erp.montfortuganda.school;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Integer branchId;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "school_code", length = 10)
    private String schoolCode;

    @Column(name = "branch_type", length = 50)
    private String branchType;

    @Column(name = "branch_location")
    private String branchLocation;

    @Column(name = "is_active", columnDefinition = "integer default 1")
    private Integer isActive = 1;
}