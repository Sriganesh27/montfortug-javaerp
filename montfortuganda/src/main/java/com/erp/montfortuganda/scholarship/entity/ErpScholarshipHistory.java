package com.erp.montfortuganda.scholarship.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "erp_scholarship_history",
       indexes = {
           @Index(name = "idx_scholarship_history_account", columnList = "scholarship_app_id"),
           @Index(name = "idx_scholarship_history_student", columnList = "student_id"),
           @Index(name = "idx_scholarship_history_branch", columnList = "branch_id"),
           @Index(name = "idx_scholarship_history_application", columnList = "application_id"),
           @Index(name = "idx_scholarship_history_year", columnList = "academic_year"),
           @Index(name = "idx_scholarship_history_term", columnList = "term_requested"),
           @Index(name = "idx_scholarship_history_status", columnList = "status"),
           @Index(name = "idx_scholarship_history_stage", columnList = "current_stage"),
           @Index(name = "idx_scholarship_history_submitted", columnList = "submitted_at")
       })
@Getter
@Setter
@NoArgsConstructor
public class ErpScholarshipHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scholarship_history_id")
    private Long scholarshipHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scholarship_app_id", nullable = false)
    private ErpScholarshipApplication scholarshipApplication;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "term_requested", nullable = false, length = 50)
    private String termRequested = "TERM_1";

    @Column(name = "category", nullable = false, length = 100)
    private String category = "GENERAL";

    @Enumerated(EnumType.STRING)
    @Column(name = "scholarship_type", length = 30)
    private ScholarshipType scholarshipType = ScholarshipType.OTHER;

    @Column(name = "application_method", nullable = false, length = 30)
    private String applicationMethod = "SCHOOL_ASSISTED";

    @Column(name = "amount_requested_ugx", nullable = false, precision = 38, scale = 2)
    private BigDecimal amountRequestedUgx = BigDecimal.ZERO;

    @Column(name = "requested_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal requestedPercentage;

    @Column(name = "approved_percentage", precision = 5, scale = 2)
    private BigDecimal approvedPercentage;

    @Column(name = "approved_amount", precision = 12, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "parent_income_declared", precision = 15, scale = 2)
    private BigDecimal parentIncomeDeclared;

    @Column(name = "father_status", length = 30)
    private String fatherStatus;

    @Column(name = "father_annual_income", precision = 15, scale = 2)
    private BigDecimal fatherAnnualIncome;

    @Column(name = "mother_status", length = 30)
    private String motherStatus;

    @Column(name = "mother_annual_income", precision = 15, scale = 2)
    private BigDecimal motherAnnualIncome;

    @Column(name = "responsible_person_type", length = 30)
    private String responsiblePersonType;

    @Column(name = "responsible_person_name", length = 150)
    private String responsiblePersonName;

    @Column(name = "responsible_person_relation", length = 100)
    private String responsiblePersonRelation;

    @Column(name = "responsible_person_occupation", length = 150)
    private String responsiblePersonOccupation;

    @Column(name = "responsible_person_mobile", length = 50)
    private String responsiblePersonMobile;

    @Column(name = "responsible_person_annual_income", precision = 15, scale = 2)
    private BigDecimal responsiblePersonAnnualIncome;

    @Column(name = "orphan_status", length = 30)
    private String orphanStatus;

    @Column(name = "household_income", precision = 15, scale = 2)
    private BigDecimal householdIncome;

    @Column(name = "other_household_income", precision = 15, scale = 2)
    private BigDecimal otherHouseholdIncome;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "dependants_count")
    private Integer dependantsCount;

    @Column(name = "school_going_children")
    private Integer schoolGoingChildren;

    @Column(name = "main_income_earner", length = 150)
    private String mainIncomeEarner;

    @Column(name = "income_source", length = 255)
    private String incomeSource;

    @Column(name = "housing_status", length = 100)
    private String housingStatus;

    @Column(name = "house_type", length = 100)
    private String houseType;

    @Column(name = "house_room_count")
    private Integer houseRoomCount;

    @Column(name = "house_location", length = 255)
    private String houseLocation;

    @Column(name = "house_estimated_value", precision = 15, scale = 2)
    private BigDecimal houseEstimatedValue;

    @Column(name = "house_mortgaged", nullable = false)
    private Boolean houseMortgaged = false;

    @Column(name = "monthly_rent", precision = 15, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "land_owned", nullable = false)
    private Boolean landOwned = false;

    @Column(name = "land_area", precision = 10, scale = 2)
    private BigDecimal landArea;

    @Column(name = "land_unit", length = 30)
    private String landUnit;

    @Column(name = "land_plot_count")
    private Integer landPlotCount;

    @Column(name = "land_location", length = 255)
    private String landLocation;

    @Column(name = "land_usage", length = 100)
    private String landUsage;

    @Column(name = "land_estimated_value", precision = 15, scale = 2)
    private BigDecimal landEstimatedValue;

    @Column(name = "land_generates_income", nullable = false)
    private Boolean landGeneratesIncome = false;

    @Column(name = "land_annual_income", precision = 15, scale = 2)
    private BigDecimal landAnnualIncome;

    @Column(name = "vehicles_owned", nullable = false)
    private Boolean vehiclesOwned = false;

    @Column(name = "vehicle_count")
    private Integer vehicleCount;

    @Column(name = "vehicle_description", length = 255)
    private String vehicleDescription;

    @Column(name = "vehicle_type", length = 100)
    private String vehicleType;

    @Column(name = "vehicle_usage", length = 100)
    private String vehicleUsage;

    @Column(name = "vehicle_estimated_value", precision = 15, scale = 2)
    private BigDecimal vehicleEstimatedValue;

    @Column(name = "vehicle_financed", nullable = false)
    private Boolean vehicleFinanced = false;

    @Column(name = "business_owned", nullable = false)
    private Boolean businessOwned = false;

    @Column(name = "business_name", length = 150)
    private String businessName;

    @Column(name = "business_type", length = 150)
    private String businessType;

    @Column(name = "business_location", length = 255)
    private String businessLocation;

    @Column(name = "business_employee_count")
    private Integer businessEmployeeCount;

    @Column(name = "business_annual_income", precision = 15, scale = 2)
    private BigDecimal businessAnnualIncome;

    @Column(name = "livestock_owned", nullable = false)
    private Boolean livestockOwned = false;

    @Column(name = "livestock_description", length = 1000)
    private String livestockDescription;

    @Column(name = "livestock_estimated_value", precision = 15, scale = 2)
    private BigDecimal livestockEstimatedValue;

    @Column(name = "livestock_annual_income", precision = 15, scale = 2)
    private BigDecimal livestockAnnualIncome;

    @Column(name = "other_assets_description", length = 1000)
    private String otherAssetsDescription;

    @Column(name = "other_assets_estimated_value", precision = 15, scale = 2)
    private BigDecimal otherAssetsEstimatedValue;

    @Column(name = "other_assets_annual_income", precision = 15, scale = 2)
    private BigDecimal otherAssetsAnnualIncome;

    @Column(name = "financial_hardship_reason", columnDefinition = "TEXT")
    private String financialHardshipReason;

    @Column(name = "family_situation_remarks", length = 1000)
    private String familySituationRemarks;

    @Column(name = "parent_guardian_name", length = 150)
    private String parentGuardianName;

    @Column(name = "parent_guardian_relation", length = 100)
    private String parentGuardianRelation;

    @Column(name = "parent_guardian_mobile", length = 50)
    private String parentGuardianMobile;

    @Column(name = "declaration_accepted", nullable = false)
    private Boolean declarationAccepted = false;

    @Column(name = "verification_employee_id")
    private Long verificationEmployeeId;

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "NOT_ASSIGNED";

    @Column(name = "verification_assigned_at")
    private LocalDateTime verificationAssignedAt;

    @Column(name = "verification_started_at")
    private LocalDateTime verificationStartedAt;

    @Column(name = "verification_completed_at")
    private LocalDateTime verificationCompletedAt;

    @Column(name = "verification_remarks", length = 1000)
    private String verificationRemarks;

    @Column(name = "school_review_status", nullable = false, length = 30)
    private String schoolReviewStatus = "PENDING";

    @Column(name = "school_reviewed_by")
    private Long schoolReviewedBy;

    @Column(name = "school_reviewed_at")
    private LocalDateTime schoolReviewedAt;

    @Column(name = "school_review_remarks", length = 1000)
    private String schoolReviewRemarks;

    @Column(name = "superadmin_review_status", nullable = false, length = 30)
    private String superadminReviewStatus = "PENDING";

    @Column(name = "superadmin_reviewed_by")
    private Long superadminReviewedBy;

    @Column(name = "superadmin_reviewed_at")
    private LocalDateTime superadminReviewedAt;

    @Column(name = "superadmin_review_remarks", length = 1000)
    private String superadminReviewRemarks;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "Pending";

    @Column(name = "current_stage", length = 50)
    private String currentStage;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "reviewer_remarks", length = 500)
    private String reviewerRemarks;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (version == null) version = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ScholarshipType {
        MERIT,
        NEED_BASED,
        SPORTS,
        STAFF_CHILD,
        SIBLING,
        DONOR,
        OTHER
    }
}
