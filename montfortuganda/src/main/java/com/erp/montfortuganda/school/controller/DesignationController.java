package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import com.erp.montfortuganda.school.enums.DesignationSortField;
import com.erp.montfortuganda.school.service.DesignationService;
import com.erp.montfortuganda.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/designations")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DesignationDTO>>> searchDesignations(
            @RequestParam(required = false) Integer branchId,
            
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @RequestParam(defaultValue = "DISPLAY_ORDER") DesignationSortField sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {

        int size = Math.min(pageable.getPageSize(), 100);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), size, Sort.by(direction, sortBy.getDbField()));

        CurrentUserContext ctx = currentUserService.getCurrentUserContext(authentication);
        Page<DesignationDTO> designations = designationService.searchDesignations(
                ctx, branchId, keyword, active, createdAfter, createdBefore, safePageable);

        return ResponseEntity.ok(ApiResponse.success("Designations retrieved successfully", designations));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DesignationDTO>> createDesignation(
            @Valid @RequestBody DesignationDTO dto,
            Authentication authentication) {

        CurrentUserContext ctx = currentUserService.getCurrentUserContext(authentication);
        DesignationDTO created = designationService.createDesignation(dto, ctx);
        return ResponseEntity.ok(ApiResponse.success("Designation created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DesignationDTO>> updateDesignation(
            @PathVariable Long id,
            @Valid @RequestBody DesignationDTO dto,
            Authentication authentication) {

        CurrentUserContext ctx = currentUserService.getCurrentUserContext(authentication);
        DesignationDTO updated = designationService.updateDesignation(id, dto, ctx);
        return ResponseEntity.ok(ApiResponse.success("Designation updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateDesignation(
            @PathVariable Long id,
            Authentication authentication) {

        CurrentUserContext ctx = currentUserService.getCurrentUserContext(authentication);
        designationService.deactivateDesignation(id, ctx);
        return ResponseEntity.ok(ApiResponse.success("Designation deactivated successfully", null));
    }
}
