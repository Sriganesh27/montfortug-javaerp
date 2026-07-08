package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationSummaryDTO;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import org.springframework.data.domain.Page;

public interface BranchAdmissionService {
    // Pure business context! No Principal, no JPA Entity.
    Page<ApplicationSummaryDTO> getBranchApplications(CurrentUserContext ctx, int page, int size);
}