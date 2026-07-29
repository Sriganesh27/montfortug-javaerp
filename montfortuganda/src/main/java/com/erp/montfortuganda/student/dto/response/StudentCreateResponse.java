package com.erp.montfortuganda.student.dto.response;

import java.time.LocalDateTime;

/**
 * Response returned after a Student and initial enrollment
 * are created successfully.
 */
public record StudentCreateResponse(

        Long studentId,

        Long enrollmentId,

        String admissionNo,

        String studentCode,

        String fullName,

        Integer branchId,

        Long academicYearId,

        Integer classId,

        Long sectionId,

        String rollNo,

        String studentStatus,

        String enrollmentStatus,

        Long version,

        LocalDateTime createdAt

) {
}