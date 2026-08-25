package com.erp.montfortuganda.scholarship.entity;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.student.entity.ErpStudent;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@DynamicUpdate
@Table(name = "erp_scholarship_applications")
@EqualsAndHashCode(exclude = {"application", "student", "documents"})
@ToString(exclude = {"application", "student", "documents"})
public class ErpScholarshipApplication implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum ScholarshipType {
        MERIT,
        NEED_BASED,
        SPORTS,
        STAFF_CHILD,
        SIBLING,
        DONOR,
        OTHER
    }

    public enum ApplicationMethod {
        EMAIL_LINK,
        SCHOOL_ASSISTED
    }

    public enum SchoolReviewStatus {
        PENDING,
        UNDER_REVIEW,
        SHORTLISTED,
        NOT_SHORTLISTED
    }

    public enum SuperAdminReviewStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        PARTIALLY_APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scholarship_app_id")
    private Long scholarshipAppId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "student_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ErpStudent student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "application_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ErpApplication application;

    @Column(
            name = "academic_year",
            length = 20,
            nullable = false
    )
    private String academicYear;

    @Column(
            name = "amount_requested_ugx",
            precision = 38,
            scale = 2,
            nullable = false
    )
    private BigDecimal amountRequestedUgx =
            BigDecimal.ZERO;

    @Column(
            name = "term_requested",
            length = 50,
            nullable = false
    )
    private String termRequested = "TERM_1";

    @Column(
            name = "category",
            length = 100,
            nullable = false
    )
    private String category = "GENERAL";

    @Enumerated(EnumType.STRING)
    @Column(name = "scholarship_type")
    private ScholarshipType scholarshipType =
            ScholarshipType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "application_method",
            length = 30,
            nullable = false
    )
    private ApplicationMethod applicationMethod =
            ApplicationMethod.SCHOOL_ASSISTED;

    /*
     * Store only a one-way hash of the public scholarship token.
     * The raw token is sent to the parent and must not be persisted.
     */
    @Column(
            name = "public_token_hash",
            length = 255
    )
    private String publicTokenHash;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "token_used_at")
    private LocalDateTime tokenUsedAt;

    /*
     * School-assisted scholarship launch access.
     *
     * The browser receives only the raw opaque launch key once. Only its
     * SHA-256 hash is persisted here. It is intentionally separate from the
     * public parent token so a school key can never be accepted as a public
     * token, or vice versa.
     */
    @Column(
            name = "school_access_token_hash",
            length = 64
    )
    private String schoolAccessTokenHash;

    @Column(name = "school_access_expires_at")
    private LocalDateTime schoolAccessExpiresAt;

    @Column(name = "school_access_issued_at")
    private LocalDateTime schoolAccessIssuedAt;

    @Column(name = "school_access_issued_by")
    private Long schoolAccessIssuedBy;

    @Column(
            name = "requested_percentage",
            precision = 5,
            scale = 2,
            nullable = false
    )
    private BigDecimal requestedPercentage =
            BigDecimal.ZERO;

    @Column(
            name = "approved_percentage",
            precision = 5,
            scale = 2
    )
    private BigDecimal approvedPercentage;

    @Column(
            name = "approved_amount",
            precision = 12,
            scale = 2
    )
    private BigDecimal approvedAmount;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(
            name = "parent_income_declared",
            precision = 15,
            scale = 2
    )
    private BigDecimal parentIncomeDeclared;

    @Column(
            name = "father_status",
            length = 30
    )
    private String fatherStatus;

    @Column(
            name = "father_annual_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal fatherAnnualIncome;

    @Column(
            name = "mother_status",
            length = 30
    )
    private String motherStatus;

    @Column(
            name = "mother_annual_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal motherAnnualIncome;

    @Column(
            name = "responsible_person_type",
            length = 30
    )
    private String responsiblePersonType;

    @Column(
            name = "responsible_person_name",
            length = 150
    )
    private String responsiblePersonName;

    @Column(
            name = "responsible_person_relation",
            length = 100
    )
    private String responsiblePersonRelation;

    @Column(
            name = "responsible_person_occupation",
            length = 150
    )
    private String responsiblePersonOccupation;

    @Column(
            name = "responsible_person_mobile",
            length = 50
    )
    private String responsiblePersonMobile;

    @Column(
            name = "responsible_person_annual_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal responsiblePersonAnnualIncome;

    @Column(
            name = "orphan_status",
            length = 30
    )
    private String orphanStatus;

    @Column(
            name = "household_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal householdIncome;

    @Column(
            name = "other_household_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal otherHouseholdIncome;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "dependants_count")
    private Integer dependantsCount;

    @Column(name = "school_going_children")
    private Integer schoolGoingChildren;

    @Column(
            name = "main_income_earner",
            length = 150
    )
    private String mainIncomeEarner;

    @Column(
            name = "income_source",
            length = 255
    )
    private String incomeSource;

    @Column(
            name = "housing_status",
            length = 100
    )
    private String housingStatus;

    /*
     * Detailed housing information.
     *
     * housingStatus remains the primary ownership/occupancy value
     * (for example OWNED, RENTED, RELATIVE_HOME, EMPLOYER_HOUSING, OTHER).
     * The fields below are populated dynamically according to that choice.
     */
    @Column(
            name = "house_type",
            length = 100
    )
    private String houseType;

    @Column(name = "house_room_count")
    private Integer houseRoomCount;

    @Column(
            name = "house_location",
            length = 255
    )
    private String houseLocation;

    @Column(
            name = "house_estimated_value",
            precision = 15,
            scale = 2
    )
    private BigDecimal houseEstimatedValue;

    @Column(
            name = "house_mortgaged",
            nullable = false
    )
    private Boolean houseMortgaged = false;

    @Column(
            name = "monthly_rent",
            precision = 15,
            scale = 2
    )
    private BigDecimal monthlyRent;

    @Column(
            name = "land_owned",
            nullable = false
    )
    private Boolean landOwned = false;

    @Column(
            name = "land_area",
            precision = 10,
            scale = 2
    )
    private BigDecimal landArea;

    @Column(
            name = "land_unit",
            length = 30
    )
    private String landUnit;

    @Column(name = "land_plot_count")
    private Integer landPlotCount;

    @Column(
            name = "land_location",
            length = 255
    )
    private String landLocation;

    @Column(
            name = "land_usage",
            length = 100
    )
    private String landUsage;

    @Column(
            name = "land_estimated_value",
            precision = 15,
            scale = 2
    )
    private BigDecimal landEstimatedValue;

    @Column(
            name = "land_generates_income",
            nullable = false
    )
    private Boolean landGeneratesIncome = false;

    @Column(
            name = "land_annual_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal landAnnualIncome;

    @Column(
            name = "vehicles_owned",
            nullable = false
    )
    private Boolean vehiclesOwned = false;

    @Column(name = "vehicle_count")
    private Integer vehicleCount;

    @Column(
            name = "vehicle_description",
            length = 255
    )
    private String vehicleDescription;

    @Column(
            name = "vehicle_type",
            length = 100
    )
    private String vehicleType;

    @Column(
            name = "vehicle_usage",
            length = 100
    )
    private String vehicleUsage;

    @Column(
            name = "vehicle_estimated_value",
            precision = 15,
            scale = 2
    )
    private BigDecimal vehicleEstimatedValue;

    @Column(
            name = "vehicle_financed",
            nullable = false
    )
    private Boolean vehicleFinanced = false;

    /*
     * Family business / income-generating activity.
     */
    @Column(
            name = "business_owned",
            nullable = false
    )
    private Boolean businessOwned = false;

    @Column(
            name = "business_name",
            length = 150
    )
    private String businessName;

    @Column(
            name = "business_type",
            length = 150
    )
    private String businessType;

    @Column(
            name = "business_location",
            length = 255
    )
    private String businessLocation;

    @Column(name = "business_employee_count")
    private Integer businessEmployeeCount;

    @Column(
            name = "business_annual_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal businessAnnualIncome;

    /*
     * Livestock / farming assets.
     */
    @Column(
            name = "livestock_owned",
            nullable = false
    )
    private Boolean livestockOwned = false;

    @Column(
            name = "livestock_description",
            length = 1000
    )
    private String livestockDescription;

    @Column(
            name = "livestock_estimated_value",
            precision = 15,
            scale = 2
    )
    private BigDecimal livestockEstimatedValue;

    @Column(
            name = "livestock_annual_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal livestockAnnualIncome;

    /*
     * Other material assets/investments not covered above.
     */
    @Column(
            name = "other_assets_description",
            length = 1000
    )
    private String otherAssetsDescription;

    @Column(
            name = "other_assets_estimated_value",
            precision = 15,
            scale = 2
    )
    private BigDecimal otherAssetsEstimatedValue;

    @Column(
            name = "other_assets_annual_income",
            precision = 15,
            scale = 2
    )
    private BigDecimal otherAssetsAnnualIncome;

    @Lob
    @Column(
            name = "financial_hardship_reason",
            columnDefinition = "TEXT"
    )
    private String financialHardshipReason;

    @Column(
            name = "family_situation_remarks",
            length = 1000
    )
    private String familySituationRemarks;

    @Column(
            name = "parent_guardian_name",
            length = 150
    )
    private String parentGuardianName;

    @Column(
            name = "parent_guardian_relation",
            length = 100
    )
    private String parentGuardianRelation;

    @Column(
            name = "parent_guardian_mobile",
            length = 50
    )
    private String parentGuardianMobile;

    @Column(
            name = "declaration_accepted",
            nullable = false
    )
    private Boolean declarationAccepted = false;

    @Column(name = "verification_employee_id")
    private Long verificationEmployeeId;

    @Column(
            name = "verification_status",
            length = 30,
            nullable = false
    )
    private String verificationStatus = "NOT_ASSIGNED";

    @Column(name = "verification_assigned_at")
    private LocalDateTime verificationAssignedAt;

    @Column(name = "verification_started_at")
    private LocalDateTime verificationStartedAt;

    @Column(name = "verification_completed_at")
    private LocalDateTime verificationCompletedAt;

    @Column(
            name = "verification_remarks",
            length = 1000
    )
    private String verificationRemarks;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "school_review_status",
            length = 30,
            nullable = false
    )
    private SchoolReviewStatus schoolReviewStatus =
            SchoolReviewStatus.PENDING;

    @Column(name = "school_reviewed_by")
    private Long schoolReviewedBy;

    @Column(name = "school_reviewed_at")
    private LocalDateTime schoolReviewedAt;

    @Column(
            name = "school_review_remarks",
            length = 1000
    )
    private String schoolReviewRemarks;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "superadmin_review_status",
            length = 30,
            nullable = false
    )
    private SuperAdminReviewStatus superAdminReviewStatus =
            SuperAdminReviewStatus.PENDING;

    @Column(name = "superadmin_reviewed_by")
    private Long superAdminReviewedBy;

    @Column(name = "superadmin_reviewed_at")
    private LocalDateTime superAdminReviewedAt;

    @Column(
            name = "superadmin_review_remarks",
            length = 1000
    )
    private String superAdminReviewRemarks;

    @Column(
            name = "reason",
            length = 500
    )
    private String reason;

    @Column(
            name = "reviewer_remarks",
            length = 500
    )
    private String reviewerRemarks;

    /*
     * Keep the existing lifecycle status as String for backward compatibility
     * with the current scholarship service while the new workflow is added.
     */
    @Column(
            name = "status",
            length = 50,
            nullable = false
    )
    private String status = "Pending";

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active = true;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version = 0L;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "scholarshipApplication",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ErpScholarshipApplicationDoc> documents =
            new ArrayList<>();

    public void addDocument(
            ErpScholarshipApplicationDoc document
    ) {
        documents.add(document);
        document.setScholarshipApplication(this);
    }

    public void removeDocument(
            ErpScholarshipApplicationDoc document
    ) {
        documents.remove(document);
        document.setScholarshipApplication(null);
    }

    @PrePersist
    private void onCreate() {
        if (active == null) {
            active = true;
        }

        if (declarationAccepted == null) {
            declarationAccepted = false;
        }

        if (landOwned == null) {
            landOwned = false;
        }

        if (vehiclesOwned == null) {
            vehiclesOwned = false;
        }

        if (verificationStatus == null
                || verificationStatus.isBlank()) {
            verificationStatus = "NOT_ASSIGNED";
        }

        if (applicationMethod == null) {
            applicationMethod =
                    ApplicationMethod.SCHOOL_ASSISTED;
        }

        if (scholarshipType == null) {
            scholarshipType =
                    ScholarshipType.OTHER;
        }

        if (schoolReviewStatus == null) {
            schoolReviewStatus =
                    SchoolReviewStatus.PENDING;
        }

        if (superAdminReviewStatus == null) {
            superAdminReviewStatus =
                    SuperAdminReviewStatus.PENDING;
        }

        if (amountRequestedUgx == null) {
            amountRequestedUgx =
                    BigDecimal.ZERO;
        }

        if (requestedPercentage == null) {
            requestedPercentage =
                    BigDecimal.ZERO;
        }

        if (termRequested == null
                || termRequested.isBlank()) {
            termRequested = "TERM_1";
        }

        if (category == null
                || category.isBlank()) {
            category = "GENERAL";
        }

        if (status == null
                || status.isBlank()) {
            status = "Pending";
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
