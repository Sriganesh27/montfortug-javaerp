package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationSummaryDTO;
import com.erp.montfortuganda.admission.dto.BranchApplicationDetailsResponseDTO;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import org.springframework.data.domain.Page;

/**
 * Branch-scoped admission application operations.
 *
 * <p>The service accepts the authenticated user context instead of raw branch
 * IDs so branch ownership is always derived and validated on the server.</p>
 */
public interface BranchAdmissionService {

    /**
     * Returns the authenticated branch's active admission applications.
     */
    Page<ApplicationSummaryDTO> getBranchApplications(
            CurrentUserContext context,
            int page,
            int size
    );

    /**
     * Returns the complete review details for one application only when it
     * belongs to the authenticated user's branch.
     */
    BranchApplicationDetailsResponseDTO getBranchApplicationDetails(
            CurrentUserContext context,
            Long applicationId
    );
}
