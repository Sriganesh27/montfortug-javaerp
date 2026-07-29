package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.student.dto.request.StudentCreateRequest;
import com.erp.montfortuganda.student.dto.request.StudentEnrollmentUpdateRequest;
import com.erp.montfortuganda.student.dto.request.StudentListFilterRequest;
import com.erp.montfortuganda.student.dto.request.StudentStatusChangeRequest;
import com.erp.montfortuganda.student.dto.request.StudentUpdateRequest;
import com.erp.montfortuganda.student.dto.response.PagedStudentResponse;
import com.erp.montfortuganda.student.dto.response.StudentCreateResponse;
import com.erp.montfortuganda.student.dto.response.StudentEnrollmentResponse;
import com.erp.montfortuganda.student.dto.response.StudentPersonalResponse;
import com.erp.montfortuganda.student.dto.response.StudentProfileResponse;
import com.erp.montfortuganda.student.dto.response.StudentReferenceDataResponse;
import org.springframework.data.domain.Pageable;

/**
 * Core Student-module business operations.
 *
 * Student documents and profile photos are managed through
 * StudentDocumentService and StudentPhotoService.
 */
public interface StudentService {

    /**
     * Returns active reference data required by the Student forms.
     *
     * Academic years, terms, levels and classes are shared reference data.
     * Sections must be restricted to the authenticated user's branch.
     */
    StudentReferenceDataResponse getReferenceData();

    /**
     * Creates a Student together with the required parent and enrollment
     * records and optional medical, previous academic-history, hostel
     * and transport records.
     */
    StudentCreateResponse createStudent(
            StudentCreateRequest request
    );

    /**
     * Returns the complete branch-protected Student profile.
     */
    StudentProfileResponse getStudentProfile(
            Long studentId
    );

    /**
     * Returns a filtered and paginated Student list for the authenticated
     * user's branch.
     */
    PagedStudentResponse getStudents(
            StudentListFilterRequest filter,
            Pageable pageable
    );

    /**
     * Updates Student personal and parent information and optional medical
     * and academic-history information.
     *
     * Enrollment placement is updated separately.
     */
    StudentProfileResponse updateStudent(
            Long studentId,
            StudentUpdateRequest request
    );

    /**
     * Changes the Student's current academic placement or enrollment status
     * and creates an immutable enrollment-history snapshot.
     */
    StudentEnrollmentResponse updateEnrollment(
            Long studentId,
            StudentEnrollmentUpdateRequest request
    );

    /**
     * Changes the Student lifecycle status.
     */
    StudentPersonalResponse changeStudentStatus(
            Long studentId,
            StudentStatusChangeRequest request
    );
}