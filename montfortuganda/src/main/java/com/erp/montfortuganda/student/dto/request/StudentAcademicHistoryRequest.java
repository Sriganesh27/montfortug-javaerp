package com.erp.montfortuganda.student.dto.request;

import com.erp.montfortuganda.student.entity.ErpStudentAcademicHistory.SchoolType;
import jakarta.validation.constraints.Size;

/**
 * Optional academic information from the student's previous school.
 *
 * Database IDs, branch, admission number, document paths, verification
 * information, active status, audit fields and version are controlled
 * by the backend.
 */
@SuppressWarnings("unused")
public record StudentAcademicHistoryRequest(

        // ==========================================
        // PREVIOUS SCHOOL
        // ==========================================

        @Size(
                max = 255,
                message = "Former school name cannot exceed 255 characters."
        )
        String formerSchoolName,

        @Size(
                max = 50,
                message = "Former school code cannot exceed 50 characters."
        )
        String formerSchoolCode,

        @Size(
                max = 50,
                message = "Former school LIN cannot exceed 50 characters."
        )
        String formerSchoolLin,

        @Size(
                max = 255,
                message = "Former school address cannot exceed 255 characters."
        )
        String formerSchoolAddress,

        SchoolType schoolType,

        @Size(
                max = 255,
                message = "Transfer reason cannot exceed 255 characters."
        )
        String transferReason,

        // ==========================================
        // PREVIOUS ACADEMIC PLACEMENT
        // ==========================================

        @Size(
                max = 20,
                message = "Previous academic year cannot exceed 20 characters."
        )
        String previousAcademicYear,

        @Size(
                max = 50,
                message = "Previous class cannot exceed 50 characters."
        )
        String previousClass,

        @Size(
                max = 50,
                message = "Previous section cannot exceed 50 characters."
        )
        String previousSection,

        @Size(
                max = 50,
                message = "Previous stream cannot exceed 50 characters."
        )
        String previousStream,

        // ==========================================
        // PLE
        // ==========================================

        @Size(
                max = 50,
                message = "PLE index number cannot exceed 50 characters."
        )
        String pleIndexNumber,

        @Size(
                max = 20,
                message = "PLE aggregate cannot exceed 20 characters."
        )
        String pleAggregate,

        // ==========================================
        // UCE
        // ==========================================

        @Size(
                max = 50,
                message = "UCE index number cannot exceed 50 characters."
        )
        String uceIndexNumber,

        @Size(
                max = 50,
                message = "UCE result cannot exceed 50 characters."
        )
        String uceResult,

        // ==========================================
        // UACE
        // ==========================================

        @Size(
                max = 50,
                message = "UACE index number cannot exceed 50 characters."
        )
        String uaceIndexNumber,

        @Size(
                max = 50,
                message = "UACE result cannot exceed 50 characters."
        )
        String uaceResult,

        // ==========================================
        // SUBJECT PERFORMANCE
        // ==========================================

        @Size(
                max = 50000,
                message = "Subject marks data is too large."
        )
        String subjectMarks,

        @Size(
                max = 5000,
                message = "Academic-history remarks cannot exceed 5000 characters."
        )
        String remarks
) {
}