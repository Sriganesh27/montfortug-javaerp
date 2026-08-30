package com.erp.montfortuganda.scholarship.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for filtering and paginating Scholarship Applications.
 *
 * This DTO contains only list/search filters. Detailed scholarship
 * application information belongs to the profile/detail response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipApplicationSearchRequestDTO {

    private String search;

    private String status;

    private String verificationStatus;

    private Long levelId;

    private Long classId;

    private String academicYear;

    private String term;

    /**
     * Supported UI values:
     * NEW
     * EXISTING
     *
     * EXISTING represents the existing admission types
     * READMISSION and TRANSFER.
     */
    private String applicationType;

    private String gender;

    /**
     * Supported UI values:
     * ALL
     * ORPHAN
     * SINGLE_PARENT
     * TWO_PARENTS
     *
     * This is derived from the existing orphanStatus,
     * fatherStatus and motherStatus fields. It is not a
     * new database column.
     */
    private String familySituation;

    private String scholarshipStatus;

    private Long verificationEmployeeId;

    private String schoolReviewStatus;

    private String superAdminReviewStatus;

    /**
     * Zero-based page number.
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * Number of records requested per page.
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * Server-side sort field.
     * The service layer must validate/map this value.
     */
    @Builder.Default
    private String sortBy = "submittedAt";

    /**
     * ASC or DESC.
     */
    @Builder.Default
    private String sortDirection = "DESC";
}
