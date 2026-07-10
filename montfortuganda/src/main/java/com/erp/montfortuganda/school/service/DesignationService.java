package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.EntityInUseException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.exception.UnauthorizedException;
import com.erp.montfortuganda.infrastructure.enums.ModuleCode;
import com.erp.montfortuganda.infrastructure.service.DocumentSequenceService;
import com.erp.montfortuganda.school.ErpDesignation;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import com.erp.montfortuganda.school.mapper.DesignationMapper;
import com.erp.montfortuganda.school.repository.ErpDesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DesignationService {

    private final ErpDesignationRepository designationRepository;
    private final DocumentSequenceService sequenceService;
    private final ErpEmployeeRepository employeeRepository;
    private final DesignationMapper designationMapper;

    @Transactional(readOnly = true)
    public Page<DesignationDTO> searchDesignations(CurrentUserContext context, Integer requestBranchId,  String keyword, Boolean active, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        // Designations are global now, no branch filtering
        return designationRepository.searchDesignations(keyword, active, start, end, pageable);
    }

    @Transactional
    public DesignationDTO createDesignation(DesignationDTO dto, CurrentUserContext context) {

        if (designationRepository.existsByDesignationNameIgnoreCaseAndActiveTrue(dto.getDesignationName())) {
            throw new DuplicateResourceException("A designation with this name already exists.");
        }

        ErpDesignation designation = new ErpDesignation();
        // Removed branch logic
        designation.setDesignationCode(sequenceService.generateNumber(1, ModuleCode.DESIGNATION)); // Passing dummy 1 for branch or modify sequence logic if needed
        designation.setDesignationName(dto.getDesignationName());
        designation.setDescription(dto.getDescription());
        designation.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 100);
        designation.setActive(true);

        return designationMapper.toDTO(designationRepository.save(designation));
    }

    @Transactional
    public DesignationDTO updateDesignation(Long designationId, DesignationDTO dto, CurrentUserContext context) {
        ErpDesignation designation = designationRepository.findByDesignationIdAndActiveTrue(designationId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found."));

        if (!context.getRoles().contains("SUPER_ADMIN")) {
            throw new UnauthorizedException("Unauthorized access. Only SUPER_ADMIN can edit global designations.");
        }

        if (!designation.getDesignationName().equalsIgnoreCase(dto.getDesignationName()) &&
                designationRepository.existsByDesignationNameIgnoreCaseAndActiveTrue(dto.getDesignationName())) {
            throw new DuplicateResourceException("Duplicate designation name.");
        }

        designation.setDesignationName(dto.getDesignationName());
        designation.setDescription(dto.getDescription());
        designation.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 100);

        return designationMapper.toDTO(designationRepository.save(designation));
    }

    @Transactional
    public void deactivateDesignation(Long designationId, CurrentUserContext context) {
        ErpDesignation designation = designationRepository.findByDesignationIdAndActiveTrue(designationId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found."));

        if (!context.getRoles().contains("SUPER_ADMIN")) {
            throw new UnauthorizedException("Unauthorized access. Only SUPER_ADMIN can deactivate global designations.");
        }

        if (employeeRepository.countByDesignation_DesignationIdAndActiveTrue(designationId) > 0) {
            throw new EntityInUseException("Cannot deactivate designation: Contains active employees.");
        }

        designation.setActive(false);
        designationRepository.save(designation);
    }
}
