package com.erp.montfortuganda.student.dto.response;

import java.util.List;

/**
 * Complete Student profile returned by the Student View API.
 *
 * This response combines the main Student sections so the frontend
 * does not need to make several sequential API calls.
 */
public record StudentProfileResponse(

        StudentPersonalResponse personal,

        StudentParentResponse parent,

        StudentEnrollmentResponse currentEnrollment,

        List<StudentEnrollmentHistoryResponse> enrollmentHistory,

        StudentMedicalResponse medical,

        StudentAcademicHistoryResponse academicHistory,

        StudentHostelResponse hostel,

        StudentTransportResponse transport,

        List<StudentDocumentResponse> documents

) {

    public StudentProfileResponse {

        enrollmentHistory =
                enrollmentHistory == null
                        ? List.of()
                        : List.copyOf(enrollmentHistory);

        documents =
                documents == null
                        ? List.of()
                        : List.copyOf(documents);
    }
}