package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.scholarship.dto.*;
import java.time.LocalDateTime;
import java.util.List;

public interface ScholarshipService {

    /**
     * Public response for a scholarship-link issuance action.
     *
     * <p>The raw token is deliberately never returned to the browser. It is
     * generated inside the service, stored only as a SHA-256 hash, and passed
     * directly to the after-commit email event so it can exist only inside the
     * private email button URL.</p>
     */
    record PublicScholarshipLinkToken(
            LocalDateTime expiresAt,
            String status
    ) {
    }

    /**
     * One-time browser response used only to launch the authenticated
     * school-assisted scholarship form.
     *
     * <p>The raw access key is never persisted. Only its SHA-256 hash is
     * stored against the single scholarship application. The key itself must
     * be used only in the opaque school-form URL and must never be replaced
     * with an application/scholarship/branch/employee ID.</p>
     */
    record SchoolScholarshipAccessKey(
            String accessKey,
            LocalDateTime expiresAt
    ) {
    }

    /**
     * Safe validation response for an authenticated school user.
     * No internal database identifiers are exposed.
     */
    record SchoolScholarshipAccessValidation(
            boolean valid,
            LocalDateTime expiresAt,
            String scholarshipStatus
    ) {
    }
    FundsSummaryDTO getFundsSummary();
    List<DonorDTO> getAllDonors();
    List<PendingStudentDTO> getPendingStudents();
    List<BranchDemandDTO> getBranchDemands();
    List<ActiveSponsorshipDTO> getActiveSponsorships();

    void allocateToBranch(AllocationRequestDTO request);
    void allocateToStudent(AllocationRequestDTO request);

    PublicScholarshipLinkToken issuePublicApplicationToken(
            Long applicationId
    );

    /**
     * Issues/rotates a short-lived opaque key for the authenticated
     * school-assisted Scholarship Application.
     *
     * <p>The application ID is used only by the protected branch workflow
     * endpoint. It is never placed in the scholarship-form URL.</p>
     */
    SchoolScholarshipAccessKey issueSchoolApplicationAccess(
            Long applicationId
    );

    /**
     * Validates a school-assisted access key for the currently authenticated
     * branch. The key is purpose-separated from the public parent token.
     */
    SchoolScholarshipAccessValidation validateSchoolApplicationAccess(
            String accessKey
    );

    /**
     * Loads the existing scholarship application form for branch/school use.
     * Student/application/fee data is resolved server-side.
     */
    ScholarshipApplicationFormResponseDTO getApplicationFormForBranch(
            Long applicationId
    );

    /**
     * Saves the school-assisted scholarship form without submitting it.
     * This keeps the same existing scholarship application record.
     */
    ScholarshipApplicationFormResponseDTO saveApplicationFormForBranch(
            Long applicationId,
            ScholarshipApplicationFormRequestDTO request
    );

    /**
     * Final submission of the school-assisted scholarship form.
     * The existing scholarship application moves to SUBMITTED.
     */
    ScholarshipApplicationFormResponseDTO submitApplicationFormForBranch(
            Long applicationId,
            ScholarshipApplicationFormRequestDTO request
    );


    ScholarshipApplicationFormResponseDTO getApplicationFormForSchoolAccess(
            String accessKey
    );

    ScholarshipApplicationFormResponseDTO saveApplicationFormForSchoolAccess(
            String accessKey,
            ScholarshipApplicationFormRequestDTO request
    );

    ScholarshipApplicationFormResponseDTO submitApplicationFormForSchoolAccess(
            String accessKey,
            ScholarshipApplicationFormRequestDTO request
    );

    ScholarshipApplicationFormResponseDTO getApplicationFormForPublicToken(
            String publicToken
    );

    ScholarshipApplicationFormResponseDTO saveApplicationFormForPublicToken(
            String publicToken,
            ScholarshipApplicationFormRequestDTO request
    );

    ScholarshipApplicationFormResponseDTO submitApplicationFormForPublicToken(
            String publicToken,
            ScholarshipApplicationFormRequestDTO request
    );
}