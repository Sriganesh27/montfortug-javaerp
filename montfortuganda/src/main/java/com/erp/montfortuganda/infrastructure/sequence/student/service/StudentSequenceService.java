package com.erp.montfortuganda.infrastructure.sequence.student.service;

import com.erp.montfortuganda.infrastructure.sequence.student.entity.ErpStudentSequence;
import com.erp.montfortuganda.infrastructure.sequence.student.repository.StudentSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentSequenceService {

    private final StudentSequenceRepository studentSequenceRepository;

    public StudentSequenceService(
            StudentSequenceRepository studentSequenceRepository
    ) {
        this.studentSequenceRepository = studentSequenceRepository;
    }

    /**
     * Gets the next sequence for a branch, module and running year.
     *
     * If the sequence row does not exist, firstSequence is used as the
     * initial value. StudentNumberService calculates firstSequence from
     * existing admission numbers so historical data is preserved.
     *
     * The surrounding Student transaction already locks the branch.
     * PESSIMISTIC_WRITE additionally protects an existing sequence row.
     */
    @Transactional
    public long nextSequence(
            Integer branchId,
            String moduleCode,
            int runningYear,
            String actor,
            long firstSequence
    ) {
        ErpStudentSequence sequence =
                studentSequenceRepository
                        .findForUpdate(
                                branchId.longValue(),
                                moduleCode,
                                runningYear
                        )
                        .orElse(null);

        if (sequence == null) {
            sequence = new ErpStudentSequence();
            sequence.setBranchId(branchId.longValue());
            sequence.setModuleCode(moduleCode);
            sequence.setRunningYear(runningYear);
            sequence.setCurrentSequence(
                    Math.max(firstSequence, 1L)
            );
            sequence.setActive(true);
            sequence.setDeleted(false);
            sequence.setCreatedBy(actor);
            sequence.setUpdatedBy(actor);

            studentSequenceRepository.saveAndFlush(sequence);

            return sequence.getCurrentSequence();
        }

        long nextSequence =
                sequence.getCurrentSequence() + 1L;

        sequence.setCurrentSequence(nextSequence);
        sequence.setActive(true);
        sequence.setDeleted(false);
        sequence.setUpdatedBy(actor);

        studentSequenceRepository.saveAndFlush(sequence);

        return nextSequence;
    }
}
