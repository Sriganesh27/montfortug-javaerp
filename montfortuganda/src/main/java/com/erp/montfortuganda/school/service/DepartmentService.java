package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.EntityInUseException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.exception.UnauthorizedException;
import com.erp.montfortuganda.infrastructure.enums.ModuleCode;
import com.erp.montfortuganda.infrastructure.service.DocumentSequenceService;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.ErpDepartment;
import com.erp.montfortuganda.school.dto.DepartmentDTO;
import com.erp.montfortuganda.school.enums.DepartmentType;
import com.erp.montfortuganda.school.mapper.DepartmentMapper;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.ErpDepartmentRepository;
import com.erp.montfortuganda.school.repository.ErpDesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final ErpDepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final DocumentSequenceService sequenceService;
    private final ErpDesignationRepository designationRepository;
    private final ErpEmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional(readOnly = true)
    public Page<DepartmentDTO> searchDepartments(CurrentUserContext context, Integer requestBranchId, String keyword, String departmentCode, DepartmentType type, Boolean active, LocalDateTime start, LocalDateTime end, Pageable pageable) {

        Integer targetBranchId = requestBranchId;

        // Branch Admins cannot search outside their assigned branch
        if (!context.getRoles().contains("SUPER_ADMIN")) {
            targetBranchId = context.getBranchId();
        }

        return departmentRepository.searchDepartments(targetBranchId, keyword, departmentCode, type, active, start, end, pageable)
                .map(departmentMapper::toDTO);
    }

    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO dto, CurrentUserContext context) {
        if (departmentRepository.existsByDepartmentNameIgnoreCaseAndBranch_BranchIdAndActiveTrue(dto.getDepartmentName(), context.getBranchId())) {
            throw new DuplicateResourceException("A department with this name already exists.");
        }

        Branch branch = branchRepository.findById(context.getBranchId())
                .orElseThrow(() -> new UnauthorizedException("Invalid branch context."));

        ErpDepartment department = new ErpDepartment();
        department.setBranch(branch);
        department.setDepartmentCode(sequenceService.generateNumber(context.getBranchId(), ModuleCode.DEPARTMENT));
        department.setDepartmentName(dto.getDepartmentName());
        department.setDepartmentType(dto.getDepartmentType());
        department.setDescription(dto.getDescription());
        department.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 99);
        department.setActive(true);

        return departmentMapper.toDTO(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentDTO updateDepartment(Long departmentId, DepartmentDTO dto, CurrentUserContext context) {
        ErpDepartment department = getValidatedDepartment(departmentId, context);

        departmentRepository.findByDepartmentNameIgnoreCaseAndBranch_BranchIdAndActiveTrue(dto.getDepartmentName(), context.getBranchId())
                .ifPresent(existing -> {
                    if (!existing.getDepartmentId().equals(departmentId)) {
                        throw new DuplicateResourceException("Duplicate department name.");
                    }
                });

        department.setDepartmentName(dto.getDepartmentName());
        department.setDepartmentType(dto.getDepartmentType());
        department.setDescription(dto.getDescription());
        department.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 99);

        return departmentMapper.toDTO(departmentRepository.save(department));
    }

    @Transactional
    public void deactivateDepartment(Long departmentId, CurrentUserContext context) {
        ErpDepartment department = getValidatedDepartment(departmentId, context);
        if (employeeRepository.countByDepartment_DepartmentIdAndActiveTrue(departmentId) > 0) {
            throw new EntityInUseException("Cannot deactivate department: Contains active employees.");
        }

        department.setActive(false);
        // department.setDeleted(true); // If consistent across your ERP
        departmentRepository.save(department);
    }

    private ErpDepartment getValidatedDepartment(Long departmentId, CurrentUserContext context) {
        ErpDepartment department = departmentRepository.findByDepartmentIdAndActiveTrue(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));

        if (!context.getRoles().contains("SUPER_ADMIN") && !department.getBranch().getBranchId().equals(context.getBranchId())) {
            throw new UnauthorizedException("Unauthorized access.");
        }
        return department;
    }
}

