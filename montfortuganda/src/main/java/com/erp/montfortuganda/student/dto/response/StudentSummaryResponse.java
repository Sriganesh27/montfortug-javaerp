package com.erp.montfortuganda.student.dto.response;

/**
 * Compact response used by the paginated Student List.
 *
 * This response contains only the information required by the list screen.
 * Full parent, medical, document and academic-history information must be
 * returned only through the Student Profile API.
 */
public record StudentSummaryResponse(

        Long studentId,

        String admissionNo,

        String learnerLin,

        String fullName,

        String gender,

        Integer admissionYear,

        Integer joiningClassId,

        Long joiningTermId,

        /*
         * Public API URL for the Student photo.
         * Never expose the physical server file path.
         */
        String photoUrl,

        Integer branchId,

        String branchName,

        Long academicYearId,

        String academicYearName,

        Integer classId,

        String className,

        Long sectionId,

        String sectionName,

        String rollNo,

        String preferredContactType,

        String preferredContactName,

        String preferredContactPhone,

        String studentStatus,

        String enrollmentStatus,

        Boolean active,

        Long version

) {
}