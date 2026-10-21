package com.erp.montfortuganda.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "erp_classes")
@Data
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Integer classId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "level_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude

    private Level level;

    /*
     * Class codes are unique within a branch and academic year, not globally.
     * The database migration will replace the legacy global unique index.
     */
    @Column(name = "class_code", nullable = false)
    private String classCode;

    /* Nullable during the staged migration so existing rows remain readable.
       The backfill must complete before these columns become NOT NULL. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_school_class_branch"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", foreignKey = @ForeignKey(name = "fk_school_class_academic_year"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ErpAcademicYear academicYear;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "status", columnDefinition = "integer default 1")
    private Integer status = 1;
    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ErpSection> sections = new java.util.ArrayList<>();

    public void addSection(ErpSection section) {
        sections.add(section);
        section.setSchoolClass(this);
    }
}