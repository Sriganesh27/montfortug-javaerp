package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.admission.service.BranchAdmissionService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/admission/branch")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class BranchAdmissionController {

    private final BranchAdmissionService admissionService;
    private final CurrentUserService currentUserService;

    public BranchAdmissionController(BranchAdmissionService admissionService, CurrentUserService currentUserService) {
        this.admissionService = admissionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<Object>> getBranchApplications(
            Authentication principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Controller bridges Security (Principal) -> Context -> Service
        CurrentUserContext ctx = currentUserService.getCurrentUserContext(principal);
        return ResponseEntity.ok(
                ApiResponse.success("Applications fetched", admissionService.getBranchApplications(ctx, page, size))
        );
    }
}