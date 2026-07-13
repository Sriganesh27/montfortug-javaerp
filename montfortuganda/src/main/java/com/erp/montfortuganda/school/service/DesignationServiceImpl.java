package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.common.response.PagedResponse;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import com.erp.montfortuganda.school.entity.Designation;
import com.erp.montfortuganda.school.mapper.DesignationMapper;
import com.erp.montfortuganda.school.repository.DesignationRepository;
import com.erp.montfortuganda.school.repository.DesignationSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DesignationMapper designationMapper;

    @Override
    @Transactional
    public DesignationDTO createDesignation(DesignationDTO dto) {
        if (designationRepository.findByDesignationCode(dto.getDesignationCode()).isPresent() ||
                designationRepository.findByDesignationName(dto.getDesignationName()).isPresent()) {
            throw new DataIntegrityViolationException("Designation Code or Name already exists");
        }

        Designation entity = designationMapper.toEntity(dto);
        Designation saved = designationRepository.save(entity);
        return designationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public DesignationDTO updateDesignation(Long id, DesignationDTO dto) {
        Designation entity = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with id: " + id));

        // Optimistic Locking Check
        if (!entity.getVersion().equals(dto.getVersion())) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(Designation.class, id);
        }

        designationMapper.updateEntityFromDto(dto, entity);
        Designation saved = designationRepository.save(entity);
        return designationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationDTO getDesignationById(Long id) {
        Designation entity = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with id: " + id));
        return designationMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DesignationDTO> searchDesignations(String keyword, Integer branchId, RecordStatus status, Boolean active, Pageable pageable) {
        Specification<Designation> spec = DesignationSpecification.getSearchSpecification(keyword, status, active);
        Page<DesignationDTO> page = designationRepository.findAll(spec, pageable).map(designationMapper::toDto);

        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}