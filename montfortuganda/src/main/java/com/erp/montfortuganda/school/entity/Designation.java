package com.erp.montfortuganda.school.entity;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(
        name = "erp_designations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_designation_code", columnNames = {"designation_code"}),
                @UniqueConstraint(name = "uk_designation_name", columnNames = {"designation_name"})
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class Designation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designation_id")
    private Long designationId;

    @Column(name = "designation_code", nullable = false, length = 20)
    private String designationCode;

    @Column(name = "designation_name", nullable = false, length = 100)
    private String designationName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "active", nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}