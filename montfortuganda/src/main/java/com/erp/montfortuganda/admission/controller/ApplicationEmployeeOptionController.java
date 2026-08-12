package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationEmployeeOptionDTO;
import com.erp.montfortuganda.admission.service.ApplicationEmployeeOptionService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Branch-scoped employee options for Admission workflow assignments.
 *
 * <p>The browser does not supply a branch ID. Eligible employees are resolved
 * from the authenticated user's branch by the service layer.</p>
 */
@RestController
@RequestMapping("/api/admission/branch/employee-options")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class ApplicationEmployeeOptionController {

    private final ApplicationEmployeeOptionService employeeOptionService;
    private final CurrentUserService currentUserService;

    public ApplicationEmployeeOptionController(
            ApplicationEmployeeOptionService employeeOptionService,
            CurrentUserService currentUserService
    ) {
        this.employeeOptionService = employeeOptionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApplicationEmployeeOptionDTO>>>
    getEligibleEmployees(
            Authentication authentication
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(
                        authentication
                );

        List<ApplicationEmployeeOptionDTO> employees =
                employeeOptionService.getEligibleEmployees(
                        context
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Eligible admission employees fetched successfully",
                        employees
                )
        );
    }
}
