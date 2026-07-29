package com.erp.montfortuganda.student.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Historical snapshot of a Student's previous academic enrollment.
 *
 * Class, section and academic-year names are resolved from the
 * corresponding master tables and are not stored directly in the
 * enrollment-history table.
 */
public record StudentEnrollmentHistoryResponse(

        Long enrollmentHistoryId,

        Long studentId,

        Long enrollmentId,

        Integer branchId,

        String branchName,

        String admissionNo,

        Long academicYearId,

        String academicYearName,

        Integer classId,

        String className,

        Long sectionId,

        String sectionName,

        Long streamId,

        String streamName,

        Long houseId,

        Long hostelId,

        Long bedId,

        String rollNo,

        String admissionType,

        String promotionType,

        String enrollmentStatus,

        LocalDate joiningDate,

        LocalDate leavingDate,

        LocalDate effectiveDate,

        String changeReason,

        String remarks,

        Long approvedBy,

        String approvedByName,

        LocalDateTime approvedAt,

        Long createdBy,

        String createdByName,

        LocalDateTime createdAt

) {
}