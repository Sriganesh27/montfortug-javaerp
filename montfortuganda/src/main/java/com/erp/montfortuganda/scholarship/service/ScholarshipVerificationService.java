package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.scholarship.dto.ScholarshipApplicationFormResponseDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipFinalDecisionRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipShortlistRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipVerificationAssignmentRequestDTO;
import com.erp.montfortuganda.scholarship.dto.ScholarshipVerificationCompletionRequestDTO;

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
     * Returns the existing branch fund summary used by the current
     * Scholarship management page.
     *
     * <p>This method is intentionally kept unchanged while the new
     * Scholarship Overview calculations are introduced separately.</p>
     */
    Map<String, Object> getBranchFundSummary();

    /**
     * Returns the Scholarship Overview calculations for the authenticated
     * branch.
     *
     * <p>The requested filters are used only for the selected-period
     * section. The implementation will also provide the current academic
     * year section and the complete branch section.</p>
     *
     * <ul>
     *     <li>academicYear - selected Scholarship academic year, optional</li>
     *     <li>term - selected Scholarship term, optional</li>
     *     <li>levelId - selected Level, optional</li>
     *     <li>classId - selected Class, optional</li>
     * </ul>
     *
     * <p>If academicYear and term are not supplied, the implementation will
     * use the authenticated branch's current Academic Year and current Term.
     * A null levelId means all Levels, and a null classId means all Classes.</p>
     */
    Map<String, Object> getBranchScholarshipOverview(
            String academicYear,
            String term,
            Integer levelId,
            Integer classId
    );

    List<Map<String, Object>> getVerificationEmployees();

    /**
     * Assigns the employee responsible for Scholarship verification.
     *
     * The implementation must support both:
     * 1. continuing with the Entrance Test employee; and
     * 2. assigning another eligible employee from the same branch.
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
     * Records the authorized final Super Admin Scholarship decision.
     */
    void finalDecision(
            Long scholarshipAppId,
            ScholarshipFinalDecisionRequestDTO request
    );
}
