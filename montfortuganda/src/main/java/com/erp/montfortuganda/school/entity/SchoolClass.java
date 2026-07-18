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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude

    private Level level;

    @Column(name = "class_code", nullable = false, unique = true)
    private String classCode;

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