package com.erp.montfortuganda.student.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Complete Father, Mother, Guardian and emergency-contact information
 * returned for an authorized Student profile request.
 */
public record StudentParentResponse(

        Long parentId,

        Long studentId,

        Integer branchId,

        String admissionNo,

        // ==========================================
        // FATHER DETAILS
        // ==========================================

        String fatherName,

        String fatherUin,

        String fatherPhone,

        String fatherAlternatePhone,

        String fatherEmail,

        String fatherOccupation,

        String fatherEmployer,

        String fatherDesignation,

        BigDecimal fatherAnnualIncome,

        // ==========================================
        // MOTHER DETAILS
        // ==========================================

        String motherName,

        String motherUin,

        String motherPhone,

        String motherAlternatePhone,

        String motherEmail,

        String motherOccupation,

        String motherEmployer,

        String motherDesignation,

        BigDecimal motherAnnualIncome,

        // ==========================================
        // GUARDIAN DETAILS
        // ==========================================

        String guardianName,

        String guardianUin,

        String guardianRelationship,

        String guardianPhone,

        String guardianAlternatePhone,

        String guardianEmail,

        String guardianOccupation,

        // ==========================================
        // FAMILY AND COMMUNICATION
        // ==========================================

        String preferredContact,

        String feeResponsibility,

        Boolean parentsLivingTogether,

        // ==========================================
        // EMERGENCY CONTACT
        // ==========================================

        String emergencyContactName,

        String emergencyContactPhone,

        String emergencyContactRelationship,

        String remarks,

        Boolean active,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}