package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.school.dto.DepartmentDTO;
import com.erp.montfortuganda.school.enums.DepartmentType;
import com.erp.montfortuganda.school.enums.DepartmentSortField;
import com.erp.montfortuganda.school.service.DepartmentService;
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
@RequestMapping("/api/departments")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DepartmentDTO>>> searchDepartments(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String departmentCode,
            @RequestParam(required = false) DepartmentType type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @RequestParam(defaultValue = "DISPLAY_ORDER") DepartmentSortField sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {

        int size = Math.min(pageable.getPageSize(), 100);
        // The enum strictly controls sorting security, so ALLOWED_SORTS string array is removed
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), size, Sort.by(direction, sortBy.getDbField()));

        CurrentUserContext ctx = currentUserService.getCurrentUserContext(authentication);
        Page<DepartmentDTO> departments = departmentService.searchDepartments(
                ctx, branchId, keyword, departmentCode, type, active, createdAfter, createdBefore, safePageable);

        return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(
            @Valid @RequestBody DepartmentDTO dto,
            Authentication authentication) {

        CurrentUserContext ctx = currentUserService.getCurrentUserContext(authentication);
        DepartmentDTO created = departmentService.createDepartment(dto, ctx);
        return ResponseEntity.ok(ApiResponse.success("Department created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDTO dto,
            Authentication authentication) {

        CurrentUserContext ctx = currentUserService.getCurrentUserContext(authentication);
        DepartmentDTO updated = departmentService.updateDepartment(id, dto, ctx);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateDepartment(
            @PathVariable Long id,
            Authentication authentication) {

        CurrentUserContext ctx = currentUserService.getCurrentUserContext(authentication);
        departmentService.deactivateDepartment(id, ctx);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated successfully", null));
    }
}