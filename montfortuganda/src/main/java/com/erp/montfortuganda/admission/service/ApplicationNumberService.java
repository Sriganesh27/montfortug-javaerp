package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.infrastructure.sequence.student.service.StudentSequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationNumberService {

    private static final String APPLICATION_MODULE_CODE = "APPLICATION";

    private final StudentSequenceService studentSequenceService;

    public ApplicationNumberService(
            StudentSequenceService studentSequenceService
    ) {
        this.studentSequenceService = studentSequenceService;
    }

    @Transactional
    public long nextSequence(
            Integer branchId,
            int runningYear,
            String actor
    ) {
        return studentSequenceService.nextSequence(
                branchId,
                APPLICATION_MODULE_CODE,
                runningYear,
                actor,
                0L
        );
    }
}
