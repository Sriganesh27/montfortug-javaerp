package com.erp.montfortuganda.student.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Current academic enrollment details of a Student.
 *
 * Academic-year, class and section names are resolved from their
 * respective master tables by the service or mapper.
 */
public record StudentEnrollmentResponse(

        Long enrollmentId,

        Long studentId,

        String admissionNo,

        Integer branchId,

        String branchName,

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

        Long classTeacherId,

        String classTeacherName,

        Long feeStructureId,

        Long scholarshipId,

        Long approvedBy,

        LocalDateTime approvedAt,

        Boolean locked,

        Boolean active,

        String remarks,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}