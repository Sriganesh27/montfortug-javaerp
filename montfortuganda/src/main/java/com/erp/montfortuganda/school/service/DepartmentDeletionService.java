package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.repository.DepartmentRepository; // <--- FIXES YOUR ERROR
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentDeletionService {

    private final DepartmentRepository departmentRepository;
    private final BranchAccessService branchAccessService;
    private final CurrentUserService currentUserService;

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public void softDelete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        // Validates ownership before deletion!
        branchAccessService.validateBranchAccess(department.getBranch().getBranchId());

        department.setActive(false);
        department.setStatus(RecordStatus.INACTIVE);
        departmentRepository.save(department);

        log.info("User {} soft-deleted department {} in branch {}",
                currentUserService.getCurrentUserContext().getUsername(),
                department.getDepartmentCode(),
                department.getBranch().getBranchId()
        );
    }
}