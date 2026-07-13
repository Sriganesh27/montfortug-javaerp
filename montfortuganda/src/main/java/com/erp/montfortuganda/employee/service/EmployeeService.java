package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.employee.dto.EmployeeCreateRequest;
import com.erp.montfortuganda.employee.dto.EmployeeResponse;
import com.erp.montfortuganda.employee.dto.EmployeeSearchCriteria;
import com.erp.montfortuganda.employee.dto.EmployeeUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeCreateRequest request);
    EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateRequest request);
    EmployeeResponse getEmployeeById(Long employeeId);
    Page<EmployeeResponse> searchEmployees(EmployeeSearchCriteria criteria, Pageable pageable);
    void deleteEmployee(Long employeeId);
    
    // Add getActiveTeachers for Admission Mockups
    List<com.erp.montfortuganda.employee.dto.response.EmployeeListResponse> getActiveTeachers();
}