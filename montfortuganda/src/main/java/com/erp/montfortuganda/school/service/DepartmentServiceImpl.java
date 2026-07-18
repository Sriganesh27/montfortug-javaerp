package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.common.response.PagedResponse;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DepartmentDTO;
import com.erp.montfortuganda.school.entity.Branch;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final BranchAccessService branchAccessService;
    private final DepartmentMapper departmentMapper;
    private String getCurrentUsername() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }

        return authentication.getName();
    }
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public DepartmentDTO createDepartment(DepartmentDTO dto) {

        Branch branch = branchAccessService.getAccessibleBranch(dto.getBranchId());

        String code = dto.getDepartmentCode().trim();
        String name = dto.getDepartmentName().trim();

        if (departmentRepository
                .existsByBranch_BranchIdAndDepartmentCodeIgnoreCase(
                        branch.getBranchId(),
                        code)) {

            throw new IllegalArgumentException(
                    "Department code already exists in this branch"
            );
        }

        if (departmentRepository
                .existsByBranch_BranchIdAndDepartmentNameIgnoreCase(
                        branch.getBranchId(),
                        name)) {

            throw new IllegalArgumentException(
                    "Department name already exists in this branch"
            );
        }

        Department department = new Department();
        department.setBranch(branch);

        departmentMapper.updateEntityFromDto(dto, department);

        department = departmentRepository.save(department);

        log.info(
                "User {} created department {} in branch {}",
                getCurrentUsername(),
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with ID: " + id
                        )
                );

        Integer existingBranchId = department.getBranch().getBranchId();

        branchAccessService.validateBranchAccess(existingBranchId);

        /*
         * Do not allow the department to be moved to another branch
         * through an update request.
         */
        if (dto.getBranchId() != null &&
                !existingBranchId.equals(dto.getBranchId())) {

            throw new IllegalArgumentException(
                    "Department branch cannot be changed"
            );
        }

        String code = dto.getDepartmentCode().trim();
        String name = dto.getDepartmentName().trim();

        if (departmentRepository
                .existsByBranch_BranchIdAndDepartmentCodeIgnoreCaseAndDepartmentIdNot(
                        existingBranchId,
                        code,
                        id)) {

            throw new IllegalArgumentException(
                    "Department code already exists in this branch"
            );
        }

        if (departmentRepository
                .existsByBranch_BranchIdAndDepartmentNameIgnoreCaseAndDepartmentIdNot(
                        existingBranchId,
                        name,
                        id)) {

            throw new IllegalArgumentException(
                    "Department name already exists in this branch"
            );
        }

        departmentMapper.updateEntityFromDto(dto, department);

        department = departmentRepository.save(department);

        log.info(
                "User {} updated department {} in branch {}",
                getCurrentUsername(),
                department.getDepartmentCode(),
                existingBranchId
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