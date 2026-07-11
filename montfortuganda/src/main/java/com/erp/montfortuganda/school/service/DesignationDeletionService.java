package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Designation;
import com.erp.montfortuganda.school.repository.DesignationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignationDeletionService {

    private final DesignationRepository designationRepository;

    @Transactional
    public void softDelete(Long id) {
        Designation entity = designationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Designation not found with id: " + id));

        // 1. Mark as Inactive/Deleted
        entity.setActive(false);
        entity.setStatus(RecordStatus.INACTIVE.name());
        designationRepository.save(entity);

        // TODO: Fire Domain Events
        // eventPublisher.publishEvent(new DesignationDeletedEvent(id));

        // TODO: Handle Cascading (e.g., Unassign Employees from this Designation)
    }
}