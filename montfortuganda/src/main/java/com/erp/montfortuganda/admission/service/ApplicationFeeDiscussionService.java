package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationFeeDiscussionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationFeeDiscussionResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO;
import com.erp.montfortuganda.auth.service.CurrentUserContext;

/**
 * Branch-scoped Parent Fee Discussion operations for admission applications.
 *
 * <p>The authenticated branch is resolved from {@link CurrentUserContext}.
 * Browser-supplied branch identifiers must not be trusted.</p>
 */
public interface ApplicationFeeDiscussionService {

    /**
     * Returns the current fee discussion values for an application.
     *
     * <p>If no fee record exists yet, the implementation may return a response
     * containing zero-valued fee components for the application.</p>
     */
    ApplicationFeeDiscussionResponseDTO getFeeDiscussion(
            CurrentUserContext context,
            Long applicationId
    );

    /**
     * Creates or updates the manual fee discussion for an application.
     *
     * <p>The service calculates the total/base fee from the individual fee
     * components. The client does not provide the calculated total.</p>
     */
    ApplicationFeeDiscussionResponseDTO saveFeeDiscussion(
            CurrentUserContext context,
            Long applicationId,
            ApplicationFeeDiscussionRequestDTO request
    );

    /**
     * Explicitly finalizes Parent Fee Discussion and advances the workflow
     * according to the already-saved fee decision.
     *
     * <p>PENDING cannot be finalized. FULL_PAYMENT advances to PAYMENT.
     * PARTIAL_ASSISTANCE and FULL_ASSISTANCE advance to SCHOLARSHIP, where
     * the scholarship review process remains pending until separately
     * submitted/reviewed.</p>
     */
    ApplicationStageTransitionResponseDTO finalizeFeeDiscussion(
            CurrentUserContext context,
            Long applicationId
    );
}
