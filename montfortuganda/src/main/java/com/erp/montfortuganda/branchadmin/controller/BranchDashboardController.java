package com.erp.montfortuganda.branchadmin.controller;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.branchadmin.service.BranchDashboardService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/branchadmin/dashboard")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class BranchDashboardController {

    private final BranchDashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public BranchDashboardController(BranchDashboardService dashboardService, CurrentUserService currentUserService) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getDashboardStats(Principal principal) {
        CurrentUserContext ctx = currentUserService.getCurrentUserContext(principal);
        return ResponseEntity.ok(
                ApiResponse.success("Stats fetched successfully", dashboardService.getDashboardStats(ctx))
        );
    }
}