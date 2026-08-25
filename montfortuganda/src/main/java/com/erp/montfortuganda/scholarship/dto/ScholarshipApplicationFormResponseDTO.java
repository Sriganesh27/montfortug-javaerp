package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared scholarship application form response.
 *
 * Used by:
 * 1) Branch "Fill at School"
 * 2) Public token scholarship form
 *
 * Identity, academic and fee values are loaded server-side.
 */
@Data
public class ScholarshipApplicationFormResponseDTO {

    /* =========================================================
       Scholarship / application identity
       ========================================================= */

    private Long scholarshipAppId;
    private Long applicationId;

    private String applicationNo;
    private String studentName;
    private String className;
    private String academicYear;
    private String term;

    private String scholarshipType;
    private String applicationMethod;
    private String status;

    /* =========================================================
       Fee Discussion summary - read only in scholarship form
       ========================================================= */

    private BigDecimal totalFee;
    private BigDecimal parentContribution;
    private BigDecimal scholarshipRequiredAmount;

    /* =========================================================
       Existing admission family details
       ========================================================= */

    private String fatherName;
    private String fatherContact;
    private String fatherEmail;
    private String fatherOccupation;

    private String motherName;
    private String motherContact;
    private String motherEmail;
    private String motherOccupation;

    private String guardianName;
    private String guardianRelation;
    private String guardianContact;
    private String guardianEmail;
    private String guardianOccupation;

    /* =========================================================
       Scholarship-specific family situation
       ========================================================= */

    private String fatherStatus;
    private BigDecimal fatherAnnualIncome;

    private String motherStatus;
    private BigDecimal motherAnnualIncome;

    private String responsiblePersonType;
    private String responsiblePersonName;
    private String responsiblePersonRelation;
    private String responsiblePersonOccupation;
    private String responsiblePersonMobile;
    private BigDecimal responsiblePersonAnnualIncome;

    private String orphanStatus;

    /* =========================================================
       Family income / household
       ========================================================= */

    private Integer householdSize;
    private Integer dependantsCount;
    private Integer schoolGoingChildren;

    private String mainIncomeEarner;
    private String incomeSource;

    private BigDecimal otherHouseholdIncome;

    /**
     * Calculated total annual family income.
     * This is read-only in the form.
     */
    private BigDecimal householdIncome;

    private String housingStatus;

    /* =========================================================
       Family assets - Housing
       ========================================================= */

    private String houseType;
    private Integer houseRoomCount;
    private String houseLocation;
    private BigDecimal houseEstimatedValue;
    private Boolean houseMortgaged;
    private BigDecimal monthlyRent;

    /* =========================================================
       Family assets - Land
       ========================================================= */

    private Boolean landOwned;
    private BigDecimal landArea;
    private String landUnit;
    private Integer landPlotCount;
    private String landLocation;
    private String landUsage;
    private BigDecimal landEstimatedValue;
    private Boolean landGeneratesIncome;
    private BigDecimal landAnnualIncome;

    /* =========================================================
       Family assets - Vehicles
       ========================================================= */

    private Boolean vehiclesOwned;
    private Integer vehicleCount;
    private String vehicleDescription;
    private String vehicleType;
    private String vehicleUsage;
    private BigDecimal vehicleEstimatedValue;
    private Boolean vehicleFinanced;

    /* =========================================================
       Family assets - Business
       ========================================================= */

    private Boolean businessOwned;
    private String businessName;
    private String businessType;
    private String businessLocation;
    private Integer businessEmployeeCount;
    private BigDecimal businessAnnualIncome;

    /* =========================================================
       Family assets - Livestock / Farming
       ========================================================= */

    private Boolean livestockOwned;
    private String livestockDescription;
    private BigDecimal livestockEstimatedValue;
    private BigDecimal livestockAnnualIncome;

    /* =========================================================
       Family assets - Other material assets
       ========================================================= */

    private String otherAssetsDescription;
    private BigDecimal otherAssetsEstimatedValue;
    private BigDecimal otherAssetsAnnualIncome;

    /* =========================================================
       Scholarship reason / family remarks
       ========================================================= */

    private String financialHardshipReason;
    private String familySituationRemarks;

    /* =========================================================
       Declaration / submission
       ========================================================= */

    private String parentGuardianName;
    private String parentGuardianRelation;
    private String parentGuardianMobile;
    private Boolean declarationAccepted;

    private LocalDateTime submittedAt;

    /* =========================================================
       Siblings
       ========================================================= */

    private List<SiblingResponse> siblings = new ArrayList<>();

    @Data
    public static class SiblingResponse {

        private Long siblingId;

        private String siblingName;
        private Integer age;

        private String currentStatus;

        private String classOrCourse;
        private String institution;

        private String occupation;
        private BigDecimal annualIncome;
    }
}
