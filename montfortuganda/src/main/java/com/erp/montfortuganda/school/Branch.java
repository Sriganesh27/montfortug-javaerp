package com.erp.montfortuganda.school;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true, exclude = {"branchLevels", "sections"})
@ToString(exclude = {"branchLevels", "sections"})
@Entity
@Table(name = "erp_branches")
@Data
public class Branch extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Integer branchId;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "school_code", length = 10, unique = true)
    private String schoolCode;

    // REPLACE branch_type String with the List
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BranchLevel> branchLevels = new ArrayList<>();

    @Column(name = "branch_location")
    private String branchLocation;

    @Column(name = "contact_details", columnDefinition = "TEXT")
    private String contactDetails;

    @Column(name = "incharge_details", columnDefinition = "TEXT")
    private String inchargeDetails;

    @Column(name = "school_photo_url")
    private String schoolPhotoUrl;

    @Column(name = "gov_document_url")
    private String govDocumentUrl;

    @Column(name = "foundation_date")
    private String foundationDate;

    @Column(name = "is_active", columnDefinition = "integer default 1")
    private Integer isActive = 1;

    // Helper methods
    public void addLevel(Level level, String createdBy) {
        BranchLevel branchLevel = new BranchLevel();
        branchLevel.setBranch(this);
        branchLevel.setLevel(level);
        branchLevel.setCreatedBy(createdBy);
        this.branchLevels.add(branchLevel);
    }
    // ==========================================
    // SECTIONS (e.g., A, B, North, South)
    // ==========================================

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ErpSection> sections = new java.util.ArrayList<>();

    public void addSection(ErpSection section) {
        sections.add(section);
        section.setBranch(this);
    }
    // ==========================================
    // DEPARTMENTS
    // ==========================================

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ErpDepartment> departments = new java.util.ArrayList<>();

    public void addDepartment(ErpDepartment department) {
        departments.add(department);
        department.setBranch(this);
    }
}