package com.erp.montfortuganda.school;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
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

    @Column(name = "branch_type")
    private String branchType;

    @Column(name = "branch_location")
    private String branchLocation;

    @Column(name = "contact_details", columnDefinition = "TEXT")
    private String contactDetails;

    @Column(name = "incharge_details", columnDefinition = "TEXT")
    private String inchargeDetails; // Stores the JSON string

    @Column(name = "school_photo_url")
    private String schoolPhotoUrl;

    @Column(name = "gov_document_url")
    private String govDocumentUrl;

    @Column(name = "foundation_date")
    private String foundationDate; // Use String to easily store "YYYY-MM-DD"

    @Column(name = "is_active", columnDefinition = "integer default 1")
    private Integer isActive = 1;
}