package com.erp.montfortuganda.student.dto.response;

import java.time.LocalDateTime;

/**
 * Previous-school and examination history of a Student.
 *
 * Physical document paths are never exposed directly.
 * Protected download URLs are returned instead.
 */
public record StudentAcademicHistoryResponse(

        Long academicHistoryId,

        Long studentId,

        Integer branchId,

        String admissionNo,

        // Previous school
        String formerSchoolName,

        String formerSchoolCode,

        String formerSchoolLin,

        String formerSchoolAddress,

        String schoolType,

        String transferReason,

        // Previous academic placement
        String previousAcademicYear,

        String previousClass,

        String previousSection,

        String previousStream,

        // PLE
        String pleIndexNumber,

        String pleAggregate,

        // UCE
        String uceIndexNumber,

        String uceResult,

        // UACE
        String uaceIndexNumber,

        String uaceResult,

        /**
         * JSON or structured text containing subject-level marks.
         */
        String subjectMarks,

        /**
         * Protected API URLs instead of physical file paths.
         */
        String previousReportCardUrl,

        String transferCertificateUrl,

        String leavingCertificateUrl,

        String verificationStatus,

        Long verifiedBy,

        String verifiedByName,

        LocalDateTime verifiedAt,

        Boolean active,

        String remarks,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}