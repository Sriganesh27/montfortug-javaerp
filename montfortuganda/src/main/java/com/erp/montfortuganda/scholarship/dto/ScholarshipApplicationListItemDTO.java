package com.erp.montfortuganda.scholarship.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight row DTO for the Scholarship Applications list.
 *
 * This DTO intentionally contains only data required by the applications
 * table. The complete scholarship application is loaded separately when
 * the user opens an application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipApplicationListItemDTO {

    private Long applicationId;

    /**
     * Actual number displayed to the user.
     *
     * NEW:
     *     Admission application's actual application number.
     *
     * EXISTING:
     *     Existing student's actual admission number.
     *
     * This value must always be resolved from the existing Admission/Student
     * record. Scholarship must never generate a replacement number.
     */
    private String applicationNo;

    /**
     * Explicit Scholarship applicant type.
     *
     * Allowed values:
     * NEW
     * EXISTING
     */
    private String applicationType;

    private String studentName;

    private String gender;

    private String levelName;

    private Long levelId;

    private String className;

    private Long classId;

    /**
     * Academic year belonging to this Scholarship application/cycle.
     *
     * This is independent from the original Admission application's
     * academic_year.
     */
    private String academicYear;

    private String term;

    /**
     * Original Admission type retained for backend compatibility.
     *
     * Possible values:
     * NEW
     * READMISSION
     * TRANSFER
     */
    private String admissionType;

    /**
     * Amount requested by the applicant.
     */
    private BigDecimal amountRequestedUgx;

    private String verificationStatus;

    private String scholarshipStatus;

    private String schoolReviewStatus;

    private String superAdminReviewStatus;

    private LocalDateTime submittedAt;

    /**
     * Backend-authorized next action for the current user/application.
     */
    private String nextAction;

    /**
     * Target stage associated with the next action, when applicable.
     */
    private String nextTargetStage;

    /**
     * Indicates whether an action is currently available.
     */
    private Boolean actionAvailable;
}