package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.common.dto.PagedResponse;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DepartmentDTO;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    DepartmentDTO createDepartment(DepartmentDTO dto);
    DepartmentDTO updateDepartment(Long id, DepartmentDTO dto);
    DepartmentDTO getDepartmentById(Long id);
    PagedResponse<DepartmentDTO> searchDepartments(String keyword, Integer requestedBranchId, RecordStatus status, Boolean active, Boolean isAcademic, Pageable pageable);
}