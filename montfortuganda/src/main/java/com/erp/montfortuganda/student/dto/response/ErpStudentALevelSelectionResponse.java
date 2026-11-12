package com.erp.montfortuganda.student.dto.response;

import java.time.LocalDateTime;

/**
 * A' Level combination selection details for an applicant or student.
 *
 * Related names and subject labels are populated by the service layer.
 */
public record ErpStudentALevelSelectionResponse(

        Long selectionId,

        Long applicationId,

        Long studentId,

        Long enrollmentId,

        Integer branchId,

        Long academicYearId,

        String academicYearName,

        Integer classId,

        String className,

        Long combinationId,

        String combinationCode,

        String combinationName,

        Long principalSubject1Id,

        String principalSubject1Name,

        Long principalSubject2Id,

        String principalSubject2Name,

        Long principalSubject3Id,

        String principalSubject3Name,

        Long subsidiarySubjectId,

        String subsidiarySubjectName,

        String status,

        LocalDateTime selectedAt,

        Long selectedBy,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
