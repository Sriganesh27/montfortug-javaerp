package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.entity.ErpParent.FeeResponsibility;
import com.erp.montfortuganda.student.entity.ErpParent.PreferredContact;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Complete Father, Mother, Guardian and emergency-contact information
 * submitted during Student registration or profile editing.
 *
 * Parent ID, Student ID, branch, admission number, active status,
 * audit fields and entity version are controlled by the backend.
 */
@SuppressWarnings("unused")
public record StudentParentRequest(

        // ==========================================
        // FATHER DETAILS
        // ==========================================

        @Size(
                max = 150,
                message = "Father name cannot exceed 150 characters."
        )
        String fatherName,

        @Size(
                max = 20,
                message = "Father UIN cannot exceed 20 characters."
        )
        String fatherUin,

        @Size(
                max = 30,
                message = "Father phone cannot exceed 30 characters."
        )
        String fatherPhone,

        @Size(
                max = 30,
                message = "Father alternate phone cannot exceed 30 characters."
        )
        String fatherAlternatePhone,

        @Email(message = "Father email must be valid.")
        @Size(
                max = 150,
                message = "Father email cannot exceed 150 characters."
        )
        String fatherEmail,

        @Size(
                max = 150,
                message = "Father occupation cannot exceed 150 characters."
        )
        String fatherOccupation,

        @Size(
                max = 200,
                message = "Father employer cannot exceed 200 characters."
        )
        String fatherEmployer,

        @Size(
                max = 150,
                message = "Father designation cannot exceed 150 characters."
        )
        String fatherDesignation,

        @DecimalMin(
                value = "0.00",
                message = "Father annual income cannot be negative."
        )
        BigDecimal fatherAnnualIncome,

        // ==========================================
        // MOTHER DETAILS
        // ==========================================

        @Size(
                max = 150,
                message = "Mother name cannot exceed 150 characters."
        )
        String motherName,

        @Size(
                max = 20,
                message = "Mother UIN cannot exceed 20 characters."
        )
        String motherUin,

        @Size(
                max = 30,
                message = "Mother phone cannot exceed 30 characters."
        )
        String motherPhone,

        @Size(
                max = 30,
                message = "Mother alternate phone cannot exceed 30 characters."
        )
        String motherAlternatePhone,

        @Email(message = "Mother email must be valid.")
        @Size(
                max = 150,
                message = "Mother email cannot exceed 150 characters."
        )
        String motherEmail,

        @Size(
                max = 150,
                message = "Mother occupation cannot exceed 150 characters."
        )
        String motherOccupation,

        @Size(
                max = 200,
                message = "Mother employer cannot exceed 200 characters."
        )
        String motherEmployer,

        @Size(
                max = 150,
                message = "Mother designation cannot exceed 150 characters."
        )
        String motherDesignation,

        @DecimalMin(
                value = "0.00",
                message = "Mother annual income cannot be negative."
        )
        BigDecimal motherAnnualIncome,

        // ==========================================
        // GUARDIAN DETAILS
        // ==========================================

        @Size(
                max = 150,
                message = "Guardian name cannot exceed 150 characters."
        )
        String guardianName,

        @Size(
                max = 20,
                message = "Guardian UIN cannot exceed 20 characters."
        )
        String guardianUin,

        @Size(
                max = 100,
                message = "Guardian relationship cannot exceed 100 characters."
        )
        String guardianRelationship,

        @Size(
                max = 30,
                message = "Guardian phone cannot exceed 30 characters."
        )
        String guardianPhone,

        @Size(
                max = 30,
                message = "Guardian alternate phone cannot exceed 30 characters."
        )
        String guardianAlternatePhone,

        @Email(message = "Guardian email must be valid.")
        @Size(
                max = 150,
                message = "Guardian email cannot exceed 150 characters."
        )
        String guardianEmail,

        @Size(
                max = 150,
                message = "Guardian occupation cannot exceed 150 characters."
        )
        String guardianOccupation,

        // ==========================================
        // COMMUNICATION AND FAMILY CONTEXT
        // ==========================================

        @NotNull(message = "Preferred contact is required.")
        PreferredContact preferredContact,

        @NotNull(message = "Fee responsibility is required.")
        FeeResponsibility feeResponsibility,

        @NotNull(message = "Parents-living-together selection is required.")
        Boolean parentsLivingTogether,

        // ==========================================
        // EMERGENCY CONTACT
        // ==========================================

        @Size(
                max = 150,
                message = "Emergency contact name cannot exceed 150 characters."
        )
        String emergencyContactName,

        @Size(
                max = 30,
                message = "Emergency contact phone cannot exceed 30 characters."
        )
        String emergencyContactPhone,

        @Size(
                max = 100,
                message = "Emergency contact relationship cannot exceed 100 characters."
        )
        String emergencyContactRelationship,

        @Size(
                max = 5000,
                message = "Parent remarks cannot exceed 5000 characters."
        )
        String remarks
) {

    /**
     * At least one Father, Mother or Guardian record must contain
     * both a name and a phone number.
     */
    @AssertTrue(
            message = "Enter a name and phone number for at least one Father, Mother or Guardian."
    )
    public boolean isResponsibleContactProvided() {
        return hasNameAndPhone(fatherName, fatherPhone)
                || hasNameAndPhone(motherName, motherPhone)
                || hasNameAndPhone(guardianName, guardianPhone);
    }

    /**
     * The selected preferred contact must have complete contact details.
     */
    @AssertTrue(
            message = "The selected preferred contact must have a name and phone number."
    )
    public boolean isPreferredContactValid() {
        if (preferredContact == null) {
            return true;
        }

        return switch (preferredContact) {
            case FATHER -> hasNameAndPhone(fatherName, fatherPhone);
            case MOTHER -> hasNameAndPhone(motherName, motherPhone);
            case GUARDIAN -> hasNameAndPhone(guardianName, guardianPhone);
        };
    }

    /**
     * Guardian relationship is required whenever Guardian details are entered
     * or Guardian is selected as the preferred contact.
     */
    @AssertTrue(
            message = "Guardian relationship is required when Guardian details are provided."
    )
    public boolean isGuardianRelationshipValid() {
        boolean guardianEntered =
                hasText(guardianName)
                        || hasText(guardianPhone)
                        || preferredContact == PreferredContact.GUARDIAN
                        || feeResponsibility == FeeResponsibility.GUARDIAN;

        return !guardianEntered || hasText(guardianRelationship);
    }

    /**
     * Emergency contact fields must be supplied together.
     */
    @AssertTrue(
            message = "Emergency contact name, phone and relationship must be entered together."
    )
    public boolean isEmergencyContactComplete() {
        boolean anyEmergencyValue =
                hasText(emergencyContactName)
                        || hasText(emergencyContactPhone)
                        || hasText(emergencyContactRelationship);

        if (!anyEmergencyValue) {
            return true;
        }

        return hasText(emergencyContactName)
                && hasText(emergencyContactPhone)
                && hasText(emergencyContactRelationship);
    }

    private static boolean hasNameAndPhone(
            String name,
            String phone
    ) {
        return hasText(name) && hasText(phone);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}