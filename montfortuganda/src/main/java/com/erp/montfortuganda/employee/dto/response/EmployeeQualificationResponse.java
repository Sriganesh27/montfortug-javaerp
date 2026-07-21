package com.erp.montfortuganda.employee.dto.response;

import com.erp.montfortuganda.employee.enums.QualificationLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Complete Employee qualification data returned by authenticated,
 * branch-scoped Employee APIs.

 * The private qualification-document storage path is never exposed. The
 * response returns only whether a document exists; document access will use a
 * secured backend endpoint that verifies the Employee and branch ownership.
 */
@SuppressWarnings("unused")
public record EmployeeQualificationResponse(

        Long employeeQualificationId,

        Long employeeId,

        QualificationLevel employeeQualificationLevel,

        String customLevel,

        String employeeQualificationName,

        String employeeQualificationSpecialization,

        String employeeQualificationInstitutionName,

        String qualificationGrade,

        String employeeQualificationBoardUniversity,

        String employeeQualificationCountry,

        Integer employeeQualificationStartYear,

        Integer employeeQualificationCompletionYear,

        Integer employeeQualificationDurationMonths,

        String employeeQualificationGrade,

        BigDecimal employeeQualificationPercentage,

        BigDecimal employeeQualificationCgpa,

        String employeeQualificationCertificateNumber,

        String employeeQualificationRegistrationNumber,

        Boolean employeeQualificationDocumentAvailable,

        Boolean employeeQualificationVerified,

        Integer employeeQualificationVerifiedById,

        String employeeQualificationVerifiedByUsername,

        LocalDateTime employeeQualificationVerifiedAt,

        String employeeQualificationRemarks,

        Boolean employeeQualificationActive,

        Long version,

        String createdBy,

        LocalDateTime createdAt,

        String updatedBy,

        LocalDateTime updatedAt
) {
}