package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared scholarship application form payload.
 *
 * This DTO is intentionally used by both:
 * 1) Branch "Fill at School"
 * 2) Public token scholarship form
 *
 * Application/student/fee identity is resolved server-side and is not trusted
 * from the request body.
 */
@Data
public class ScholarshipApplicationFormRequestDTO {

    /* =========================================================
       Existing admission family details
       Prefilled from ErpApplication and editable where allowed.
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

    private Integer householdSize;
    private Integer dependantsCount;
    private Integer schoolGoingChildren;

    private String mainIncomeEarner;
    private String incomeSource;

    /**
     * Additional annual household income not already represented by
     * father/mother/responsible-person/sibling income.
     */
    private BigDecimal otherHouseholdIncome;

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
       Declaration
       ========================================================= */

    private String parentGuardianName;
    private String parentGuardianRelation;
    private String parentGuardianMobile;
    private Boolean declarationAccepted;

    /* =========================================================
       Repeating sibling details
       ========================================================= */

    private List<SiblingRequest> siblings = new ArrayList<>();

    @Data
    public static class SiblingRequest {

        /*
         * No sibling database ID is accepted from the secure browser form.
         * Sibling rows are replaced server-side from submitted values.
         */
        private String siblingName;
        private Integer age;

        /**
         * STUDYING
         * WORKING
         * STUDYING_AND_WORKING
         * OTHER
         */
        private String currentStatus;

        private String classOrCourse;
        private String institution;

        private String occupation;
        private BigDecimal annualIncome;
    }
}
