package com.erp.montfortuganda.employee.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.employee.dto.EmployeeCreateRequest;
import com.erp.montfortuganda.employee.dto.EmployeeResponse;
import com.erp.montfortuganda.employee.dto.EmployeeSearchCriteria;
import com.erp.montfortuganda.employee.dto.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/branchadmin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'HR_MANAGER')") // Adjust roles as needed
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee fetched successfully", response));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<com.erp.montfortuganda.common.response.PagedResponse<EmployeeResponse>>> searchEmployees(
            @RequestBody EmployeeSearchCriteria criteria,
            Pageable pageable) {
        Page<EmployeeResponse> responses = employeeService.searchEmployees(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", new com.erp.montfortuganda.common.response.PagedResponse<>(responses)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee removed successfully", null));
    }
    
    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<java.util.List<com.erp.montfortuganda.employee.dto.response.EmployeeListResponse>>> getActiveTeachers() {
        java.util.List<com.erp.montfortuganda.employee.dto.response.EmployeeListResponse> teachers = employeeService.getActiveTeachers();
        return ResponseEntity.ok(ApiResponse.success("Teachers fetched successfully", teachers));
    }

}