package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.common.dto.PagedResponse;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DepartmentDTO;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.mapper.DepartmentMapper;
import com.erp.montfortuganda.school.repository.DepartmentRepository;
import com.erp.montfortuganda.school.repository.DepartmentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final BranchAccessService branchAccessService;
    private final DepartmentMapper departmentMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        Branch branch = branchAccessService.getAccessibleBranch(dto.getBranchId());

        Department department = new Department();
        department.setBranch(branch);
        departmentMapper.updateEntityFromDto(dto, department);

        department = departmentRepository.save(department);

        log.info("User {} created department {} in branch {}",
                currentUserService.getCurrentUserContext().getUsername(),
                department.getDepartmentCode(),
                branch.getBranchId()
        );

        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        branchAccessService.validateBranchAccess(department.getBranch().getBranchId());

        departmentMapper.updateEntityFromDto(dto, department);
        department = departmentRepository.save(department);

        log.info("User {} updated department {} in branch {}",
                currentUserService.getCurrentUserContext().getUsername(),
                department.getDepartmentCode(),
                department.getBranch().getBranchId()
        );

        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        branchAccessService.validateBranchAccess(department.getBranch().getBranchId());

        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public PagedResponse<DepartmentDTO> searchDepartments(
            String keyword, Integer requestedBranchId, RecordStatus status, Boolean active, Boolean isAcademic, Pageable pageable) {

        Integer effectiveBranchId = branchAccessService.getAccessibleBranchId(requestedBranchId);

        Specification<Department> spec = DepartmentSpecification.getSearchSpecification(
                keyword, effectiveBranchId, status, active, isAcademic
        );

        // FIX: The repository returns Page<Department> (the entity), which we then map to DTOs.
        Page<Department> page = departmentRepository.findAll(spec, pageable);

        return new PagedResponse<>(page.map(departmentMapper::toDto));
    }
}