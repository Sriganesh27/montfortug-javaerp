package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationStageTransitionResponseDTO.AvailableTransition;
import com.erp.montfortuganda.auth.service.CurrentUserContext;

import java.util.List;

/**
 * Controlled branch-scoped admission workflow transition service.
 *
 * <p>Implementations must resolve the effective branch from the authenticated
 * user context, load the application with a pessimistic write lock, validate
 * the requested transition, save the corresponding status-history record and
 * publish any applicant notification only after the transaction commits.</p>
 */
public interface ApplicationStageTransitionService {

    /**
     * Moves one branch application through an approved workflow transition.
     *
     * @param context authenticated user context
     * @param applicationId branch application identifier
     * @param request requested transition details
     * @return authoritative saved workflow state
     */
    ApplicationStageTransitionResponseDTO transition(
            CurrentUserContext context,
            Long applicationId,
            ApplicationStageTransitionRequestDTO request
    );

    /**
     * Returns backend-approved actions for the application's current stage.
     * These values are intended for rendering the branch application profile;
     * every submitted action must still be revalidated during transition.
     *
     * @param context authenticated user context
     * @param applicationId branch application identifier
     * @return available transitions for the current saved stage
     */
    List<AvailableTransition> getAvailableTransitions(
            CurrentUserContext context,
            Long applicationId
    );
}
