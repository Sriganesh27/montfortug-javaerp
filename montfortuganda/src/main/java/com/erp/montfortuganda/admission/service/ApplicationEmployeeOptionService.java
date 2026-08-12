package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationEmployeeOptionDTO;
import com.erp.montfortuganda.auth.service.CurrentUserContext;

import java.util.List;

/**
 * Provides lightweight, branch-scoped employee options for Admission workflow
 * assignments such as School Visit and Entrance Test.
 *
 * <p>The implementation must derive Branch ownership from the authenticated
 * user context and return only employees that are active and currently have
 * ACTIVE employment status.</p>
 */
public interface ApplicationEmployeeOptionService {

    /**
     * Returns eligible employees for the authenticated Branch, ordered for
     * use in Admission workflow dropdowns.
     *
     * @param context authenticated user context
     * @return eligible employee options
     */
    List<ApplicationEmployeeOptionDTO> getEligibleEmployees(
            CurrentUserContext context
    );
}
