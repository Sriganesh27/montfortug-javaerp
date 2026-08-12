package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationSchoolVisitCompleteRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationSchoolVisitResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationSchoolVisitScheduleRequestDTO;
import com.erp.montfortuganda.admission.service.ApplicationSchoolVisitService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/admission/branch/applications/{applicationId}/school-visit"
)
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class BranchApplicationSchoolVisitController {

    private final ApplicationSchoolVisitService schoolVisitService;
    private final CurrentUserService currentUserService;

    public BranchApplicationSchoolVisitController(
            ApplicationSchoolVisitService schoolVisitService,
            CurrentUserService currentUserService
    ) {
        this.schoolVisitService = schoolVisitService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ApplicationSchoolVisitResponseDTO>>
    getSchoolVisit(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(authentication);

        ApplicationSchoolVisitResponseDTO response =
                schoolVisitService.getSchoolVisit(
                        context,
                        applicationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "School visit details fetched successfully",
                        response
                )
        );
    }

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<ApplicationSchoolVisitResponseDTO>>
    scheduleSchoolVisit(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationSchoolVisitScheduleRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(authentication);

        ApplicationSchoolVisitResponseDTO response =
                schoolVisitService.scheduleSchoolVisit(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "School visit scheduled successfully",
                        response
                )
        );
    }

    @PatchMapping("/schedule")
    public ResponseEntity<ApiResponse<ApplicationSchoolVisitResponseDTO>>
    rescheduleSchoolVisit(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationSchoolVisitScheduleRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(authentication);

        ApplicationSchoolVisitResponseDTO response =
                schoolVisitService.rescheduleSchoolVisit(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "School visit rescheduled successfully",
                        response
                )
        );
    }

    @PatchMapping("/complete")
    public ResponseEntity<ApiResponse<ApplicationSchoolVisitResponseDTO>>
    completeSchoolVisit(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody
            ApplicationSchoolVisitCompleteRequestDTO request
    ) {
        CurrentUserContext context =
                currentUserService.getCurrentUserContext(authentication);

        ApplicationSchoolVisitResponseDTO response =
                schoolVisitService.completeSchoolVisit(
                        context,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "School visit attendance recorded successfully",
                        response
                )
        );
    }
}
