package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationFormResponseDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationListItemDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationSearchRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipBranchDistributionCompletionRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipFinalDecisionRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipShortlistRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipVerificationAssignmentRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipVerificationCompletionRequestDTO;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ScholarshipVerificationService {

    /**
     * Returns branch-scoped scholarship applications for the
     * Branch Admin scholarship management page.
     */
    List<Map<String, Object>> getBranchScholarships();

    ScholarshipApplicationFormResponseDTO getBranchScholarshipDetail(
            Long scholarshipAppId
    );

    /**
     * Returns a paginated, server-side filtered list for the Scholarship
     * Applications page.
     */
    Page<ScholarshipApplicationListItemDTO> searchBranchScholarships(
            ScholarshipApplicationSearchRequestDTO request
    );

    /**
     * Returns the existing branch fund summary used by the current
     * Scholarship management page.
     */
    Map<String, Object> getBranchFundSummary();

    /**
     * Returns the Scholarship Overview calculations for the authenticated
     * branch.
     */
    Map<String, Object> getBranchScholarshipOverview(
            String academicYear,
            String term,
            Integer levelId,
            Integer classId
    );

    List<Map<String, Object>> getVerificationEmployees();

    /**
     * Allocates an amount from the authenticated Branch Admin's existing
     * Super Admin-provided branch scholarship fund to one scholarship
     * application.
     *
     * Branch ownership, student identity, academic year, term and available
     * fund balance must be resolved from trusted server-side data.
     * The request must not accept a client-supplied branch ID or donor ID.
     *
     * The implementation must prevent allocation above either the remaining
     * branch fund balance or the application's eligible scholarship amount.
     */
    void allocateBranchFundToScholarship(
            Long scholarshipAppId,
            BigDecimal amountUgx
    );

    /**
     * Confirms that Branch Admin has completed distribution for the exact
     * Scholarship History/cycle.
     */
    void completeBranchDistribution(
            Long scholarshipAppId,
            ScholarshipBranchDistributionCompletionRequestDTO request
    );

    /**
     * Assigns the employee responsible for Scholarship verification.
     */
    void assignVerificationEmployee(
            Long scholarshipAppId,
            ScholarshipVerificationAssignmentRequestDTO request
    );

    /**
     * Marks Scholarship verification as completed by the assigned employee.
     */
    void completeVerification(
            Long scholarshipAppId,
            ScholarshipVerificationCompletionRequestDTO request
    );

    /**
     * Records the authorized shortlist decision after verification.
     */
    void shortlist(
            Long scholarshipAppId,
            ScholarshipShortlistRequestDTO request
    );

    /**
     * Makes a later Branch Admin decision on a Scholarship that is currently
     * WAITLISTED.
     *
     * Allowed decisions:
     * SHORTLISTED
     * REJECTED
     *
     * WAITLISTED itself is not an email-triggering decision.
     */
    void decideFromWaitlist(
            Long scholarshipAppId,
            ScholarshipShortlistRequestDTO request
    );

    /**
     * Records the authorized final Super Admin Scholarship decision.
     */
    void finalDecision(
            Long scholarshipAppId,
            ScholarshipFinalDecisionRequestDTO request
    );
}
