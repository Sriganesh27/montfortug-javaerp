package com.erp.montfortuganda.branchadmin.service;

import com.erp.montfortuganda.branchadmin.dto.BranchDashboardStatsDTO;
import com.erp.montfortuganda.auth.service.CurrentUserContext;

public interface BranchDashboardService {
    BranchDashboardStatsDTO getDashboardStats(CurrentUserContext ctx);
}