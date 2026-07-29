package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.entity.ErpStudentEnrollment.EnrollmentStatus;
import com.erp.montfortuganda.student.enums.StudentGender;
import com.erp.montfortuganda.student.enums.StudentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Optional filters used by the paginated Student List API.
 *
 * Branch ID is intentionally excluded because the backend derives
 * the permitted branch from the authenticated user.
 *
 * Pagination and sorting should be received separately through Pageable.
 */
@SuppressWarnings("unused")
public record StudentListFilterRequest(

        @Size(
                max = 50,
                message = "Student code filter cannot exceed 50 characters."
        )
        String studentCode,

        @Size(
                max = 50,
                message = "Admission number filter cannot exceed 50 characters."
        )
        String admissionNo,

        @Size(
                max = 50,
                message = "Learner LIN filter cannot exceed 50 characters."
        )
        String learnerLin,

        @Size(
                max = 200,
                message = "Student name filter cannot exceed 200 characters."
        )
        String studentName,

        @Min(
                value = 1900,
                message = "Admission year must be 1900 or later."
        )
        @Max(
                value = 2100,
                message = "Admission year cannot exceed 2100."
        )
        Integer admissionYear,

        Long academicYearId,

        Integer classId,

        Long sectionId,

        StudentGender gender,

        StudentStatus studentStatus,

        EnrollmentStatus enrollmentStatus,

        Boolean active

) {
}