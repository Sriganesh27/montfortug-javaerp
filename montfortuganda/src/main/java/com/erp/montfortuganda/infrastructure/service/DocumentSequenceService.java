package com.erp.montfortuganda.infrastructure.service;

import com.erp.montfortuganda.model.entity.ErpDocumentSequence;
import com.erp.montfortuganda.infrastructure.enums.ModuleCode;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DocumentSequenceService {

    private final EntityManager entityManager;
    private final BranchRepository branchRepository;

    public DocumentSequenceService(EntityManager entityManager, BranchRepository branchRepository) {
        this.entityManager = entityManager;
        this.branchRepository = branchRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNumber(Long branchId, ModuleCode moduleCode) {

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        String schoolCode = branch.getSchoolCode() != null ? branch.getSchoolCode() : "XXX";
        int currentYear = moduleCode.isYearlyReset() ? LocalDate.now().getYear() : 0;
        int shortYear = currentYear % 100;

        ErpDocumentSequence seq = entityManager.createQuery(
                        "SELECT s FROM ErpDocumentSequence s WHERE s.branchId = :branchId AND s.moduleCode = :moduleCode AND s.runningYear = :year",
                        ErpDocumentSequence.class)
                .setParameter("branchId", branchId)
                .setParameter("moduleCode", moduleCode.getCode())
                .setParameter("year", currentYear)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (seq == null) {
            seq = new ErpDocumentSequence();
            seq.setBranchId(branchId);
            seq.setModuleCode(moduleCode.getCode());
            seq.setRunningYear(currentYear);
            seq.setCurrentSequence(0L);
        }

        seq.setCurrentSequence(seq.getCurrentSequence() + 1);
        if (seq.getId() == null) {
            entityManager.persist(seq);
        } else {
            entityManager.merge(seq);
        }

        String paddedSequence = String.format("%0" + moduleCode.getPadding() + "d", seq.getCurrentSequence());

        if (moduleCode.isYearlyReset()) {
            // Format: EMP-U011-26-00001
            return String.format("%s-%s-%02d-%s", moduleCode.getCode(), schoolCode, shortYear, paddedSequence);
        } else {
            // Format: EMP-U011-00001
            return String.format("%s-%s-%s", moduleCode.getCode(), schoolCode, paddedSequence);
        }
    }
}