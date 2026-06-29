package com.erp.montfortuganda.school;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_levels")
@Data
public class Level extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_id")
    private Integer levelId;

    @Column(name = "level_name", nullable = false, unique = true)
    private String levelName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "status", columnDefinition = "integer default 1")
    private Integer status = 1;

    // Links to the Classes
    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchoolClass> classes = new ArrayList<>();
}