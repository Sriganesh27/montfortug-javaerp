package com.erp.montfortuganda.school.entity;

import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(
        callSuper = true,
        exclude = {"branchLevels", "sections"}
)
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

    @Column(
            name = "school_code",
            length = 10,
            unique = true
    )
    private String schoolCode;

    /*
     * Academic levels offered by this branch.
     * This relationship replaces the removed branch_type column.
     */
    @OneToMany(
            mappedBy = "branch",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("branchLevelId ASC")
    private List<BranchLevel> branchLevels =
            new ArrayList<>();

    @Column(name = "branch_location")
    private String branchLocation;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "po_box", length = 100)
    private String poBox;

    @Column(name = "locality", length = 150)
    private String locality;

    @Column(name = "city", length = 150)
    private String city;

    @Column(name = "district", length = 150)
    private String district;

    @Column(name = "region", length = 150)
    private String region;

    @Column(name = "country", length = 100)
    private String country = "Uganda";

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    /*
     * Legacy combined contact field retained temporarily
     * for compatibility with older screens.
     */
    @Column(name = "contact_details", columnDefinition = "TEXT")
    private String contactDetails;

    @Column(name = "primary_phone", length = 30)
    private String primaryPhone;

    @Column(name = "secondary_phone", length = 30)
    private String secondaryPhone;

    /*
     * Supported values:
     * NONE, PRIMARY, SECONDARY, BOTH
     */
    @Column(name = "whatsapp_phone", length = 20)
    private String whatsappPhone = "NONE";

    @Column(name = "branch_email", length = 150)
    private String branchEmail;

    @Column(name = "email_from_name", length = 150)
    private String emailFromName;

    @Column(name = "email_reply_to", length = 150)
    private String emailReplyTo;

    @Column(name = "email_enabled")
    private Boolean emailEnabled = true;

    @Column(name = "foundation_date")
    private LocalDate foundationDate;

    @Column(name = "gov_document_url", length = 1000)
    private String govDocumentUrl;

    //noinspection SpellCheckingInspection
    @Column(name = "incharge_details", columnDefinition = "TEXT")
    private String inchargeDetails;

    @Column(name = "school_photo_url", length = 500)
    private String schoolPhotoUrl;

    @Column(name = "branch_logo_url", length = 500)
    private String branchLogoUrl;

    @Column(
            name = "is_active",
            columnDefinition = "integer default 1"
    )
    private Integer isActive = 1;

    /*
     * Sections configured under this branch,
     * for example A, B, North or South.
     */
    @OneToMany(
            mappedBy = "branch",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sectionId ASC")
    private List<ErpSection> sections =
            new ArrayList<>();

    public void addLevel(
            Level level,
            String createdBy
    ) {
        BranchLevel branchLevel = new BranchLevel();
        branchLevel.setBranch(this);
        branchLevel.setLevel(level);
        branchLevel.setCreatedBy(createdBy);

        branchLevels.add(branchLevel);
    }
}