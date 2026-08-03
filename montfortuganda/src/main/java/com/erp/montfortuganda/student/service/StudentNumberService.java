package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.Branch;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Generates concurrency-safe Student identifiers.
 *
 * Permanent Admission Number:
 * {school_code}-{original_joining_year_last_2_digits}-{joining_class_code}-{4_digit_sequence}
 * Example: U021-25-P4-0001
 *
 * The sequence is common across the school for each original joining year.
 * It does not reset per class and the generated admission number never
 * changes when the Student is promoted.
 */
@Service
public class StudentNumberService {

    private static final String STUDENT_MODULE_CODE =
            "STUDENT";

    private static final int STUDENT_SEQUENCE_PADDING =
            4;

    private static final long MAX_STUDENT_SEQUENCE =
            9_999L;

    private final EntityManager entityManager;

    public StudentNumberService(
            EntityManager entityManager
    ) {
        this.entityManager = entityManager;
    }

    /**
     * Generates the next ERP Student code.
     *
     * This method joins the surrounding Student-creation transaction. When
     * Student creation fails, the sequence increment also rolls back.
     */
    /**
     * Generates the permanent Student admission number.
     *
     * Format:
     * {schoolCode}-{joiningYearYY}-{joiningClassCode}-{sequence}
     *
     * Example:
     * U021-25-P4-0001
     *
     * The sequence is branch-wide for the original joining year and does not
     * reset per class. This method joins the surrounding registration
     * transaction, so the sequence increment rolls back when creation fails.
     */
    @Transactional
    public String generateStudentCode(
            Branch branch,
            Integer admissionYear,
            Integer joiningClassId,
            Integer authenticatedUserId
    ) {
        BranchSequenceContext context =
                requireBranchContext(
                        branch,
                        authenticatedUserId
                );

        int validatedAdmissionYear =
                validateAdmissionYear(
                        admissionYear
                );

        String joiningClassCode =
                resolveJoiningClassCode(
                        joiningClassId
                );

        lockBranch(
                context.branchId()
        );

        long firstSequence =
                resolveExistingStudentCodeBaseline(
                        context.branchId(),
                        context.schoolCode(),
                        validatedAdmissionYear
                ) + 1L;

        long sequence =
                nextSequence(
                        context.branchId(),
                        STUDENT_MODULE_CODE,
                        validatedAdmissionYear,
                        context.actor(),
                        firstSequence
                );

        if (sequence > MAX_STUDENT_SEQUENCE) {
            throw new IllegalStateException(
                    "Student admission-number sequence exceeded "
                            + MAX_STUDENT_SEQUENCE
                            + " for school "
                            + context.schoolCode()
                            + " and joining year "
                            + validatedAdmissionYear
                            + "."
            );
        }

        int shortYear =
                Math.floorMod(
                        validatedAdmissionYear,
                        100
                );

        return String.format(
                Locale.ROOT,
                "%s-%02d-%s-%0"
                        + STUDENT_SEQUENCE_PADDING
                        + "d",
                context.schoolCode(),
                shortYear,
                joiningClassCode,
                sequence
        );
    }

    // =====================================================================
    // BRANCH CONTEXT
    // =====================================================================

    private BranchSequenceContext requireBranchContext(
            Branch branch,
            Integer authenticatedUserId
    ) {
        Objects.requireNonNull(
                branch,
                "Branch is required to generate a Student identifier."
        );

        Integer branchId =
                branch.getBranchId();

        if (branchId == null || branchId <= 0) {
            throw new BadRequestException(
                    "A valid branch is required to generate a Student identifier."
            );
        }

        String schoolCode =
                normalizeSchoolCode(
                        branch.getSchoolCode()
                );

        String actor =
                authenticatedUserId != null
                        ? authenticatedUserId.toString()
                        : "SYSTEM";

        return new BranchSequenceContext(
                branchId,
                schoolCode,
                actor
        );
    }

    // =====================================================================
    // ORIGINAL JOINING YEAR
    // =====================================================================

    private int validateAdmissionYear(
            Integer admissionYear
    ) {
        if (
                admissionYear == null
                        || admissionYear < 1900
                        || admissionYear > 2100
        ) {
            throw new BadRequestException(
                    "A valid Student joining year between 1900 and 2100 "
                            + "is required to generate the admission number."
            );
        }

        return admissionYear;
    }

    // =====================================================================
    // JOINING CLASS
    // =====================================================================

    private String resolveJoiningClassCode(
            Integer joiningClassId
    ) {
        if (joiningClassId == null || joiningClassId <= 0) {
            throw new BadRequestException(
                    "A valid joining class is required to generate the Student code."
            );
        }

        List<?> results =
                entityManager
                        .createNativeQuery(
                                """
                                select class_code
                                from erp_classes
                                where class_id = :classId
                                  and status = 1
                                """
                        )
                        .setParameter(
                                "classId",
                                joiningClassId
                        )
                        .getResultList();

        if (results.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Selected joining class was not found or is inactive."
            );
        }

        Object result =
                results.getFirst();

        if (!(result instanceof String classCode)) {
            throw new IllegalStateException(
                    "Joining class code could not be resolved."
            );
        }

        String normalized =
                classCode
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (!normalized.matches("N[1-3]|P[1-7]|S[1-6]")) {
            throw new BadRequestException(
                    "Joining class code must be N1-N3, P1-P7 or S1-S6."
            );
        }

        return normalized;
    }

    // =====================================================================
    // CONCURRENCY LOCK
    // =====================================================================

    private void lockBranch(
            Integer branchId
    ) {
        List<?> results =
                entityManager
                        .createNativeQuery(
                                """
                                select branch_id
                                from erp_branches
                                where branch_id = :branchId
                                for update
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        if (results.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Branch was not found while generating the Student identifier."
            );
        }
    }

    // =====================================================================
    // SEQUENCE
    // =====================================================================

    private long nextSequence(
            Integer branchId,
            String moduleCode,
            int runningYear,
            String actor,
            long firstSequence
    ) {
        List<?> sequenceResults =
                entityManager
                        .createNativeQuery(
                                """
                                select current_sequence
                                from erp_document_sequences
                                where branch_id = :branchId
                                  and module_code = :moduleCode
                                  and running_year = :runningYear
                                for update
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .setParameter(
                                "moduleCode",
                                moduleCode
                        )
                        .setParameter(
                                "runningYear",
                                runningYear
                        )
                        .getResultList();

        if (sequenceResults.isEmpty()) {
            return createInitialSequence(
                    branchId,
                    moduleCode,
                    runningYear,
                    actor,
                    Math.max(firstSequence, 1L)
            );
        }

        Object result =
                sequenceResults.getFirst();

        if (!(result instanceof Number number)) {
            throw new IllegalStateException(
                    "Student identifier sequence contains an invalid value."
            );
        }

        long nextSequence =
                number.longValue() + 1L;

        int updatedRows =
                entityManager
                        .createNativeQuery(
                                """
                                update erp_document_sequences
                                set current_sequence = :nextSequence,
                                    deleted = 0,
                                    active = 1,
                                    updated_by = :actor,
                                    updated_at = current_timestamp,
                                    version = version + 1
                                where branch_id = :branchId
                                  and module_code = :moduleCode
                                  and running_year = :runningYear
                                """
                        )
                        .setParameter(
                                "nextSequence",
                                nextSequence
                        )
                        .setParameter(
                                "actor",
                                actor
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .setParameter(
                                "moduleCode",
                                moduleCode
                        )
                        .setParameter(
                                "runningYear",
                                runningYear
                        )
                        .executeUpdate();

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "Student identifier sequence could not be updated."
            );
        }

        return nextSequence;
    }

    private long createInitialSequence(
            Integer branchId,
            String moduleCode,
            int runningYear,
            String actor,
            long initialSequence
    ) {
        entityManager
                .createNativeQuery(
                        """
                        insert into erp_document_sequences
                        (
                            branch_id,
                            module_code,
                            running_year,
                            current_sequence,
                            deleted,
                            created_by,
                            created_at,
                            updated_by,
                            updated_at,
                            active,
                            version
                        )
                        values
                        (
                            :branchId,
                            :moduleCode,
                            :runningYear,
                            :initialSequence,
                            0,
                            :actor,
                            current_timestamp,
                            :actor,
                            current_timestamp,
                            1,
                            0
                        )
                        """
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .setParameter(
                        "moduleCode",
                        moduleCode
                )
                .setParameter(
                        "runningYear",
                        runningYear
                )
                .setParameter(
                        "initialSequence",
                        initialSequence
                )
                .setParameter(
                        "actor",
                        actor
                )
                .executeUpdate();

        return initialSequence;
    }

    /**
     * Protects a branch if the sequence table is missing but Student rows
     * already exist. The next generated permanent admission number continues
     * from the highest matching admission_no suffix.
     */
    private long resolveExistingStudentCodeBaseline(
            Integer branchId,
            String schoolCode,
            int academicYear
    ) {
        int shortYear =
                Math.floorMod(
                        academicYear,
                        100
                );

        String admissionPrefix =
                String.format(
                        Locale.ROOT,
                        "%s-%02d-%%",
                        schoolCode,
                        shortYear
                );

        List<?> results =
                entityManager
                        .createNativeQuery(
                                """
                                select coalesce(
                                           max(
                                               cast(
                                                   substring_index(
                                                       admission_no,
                                                       '-',
                                                       -1
                                                   ) as unsigned
                                               )
                                           ),
                                           0
                                       )
                                from erp_students
                                where branch_id = :branchId
                                  and admission_no like :admissionPrefix
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .setParameter(
                                "admissionPrefix",
                                admissionPrefix
                        )
                        .getResultList();

        return firstNumberOrZero(
                results
        );
    }

    private long firstNumberOrZero(
            List<?> results
    ) {
        if (
                results == null ||
                        results.isEmpty()
        ) {
            return 0L;
        }

        Object result =
                results.getFirst();

        return result instanceof Number number
                ? Math.max(number.longValue(), 0L)
                : 0L;
    }

    // =====================================================================
    // NORMALIZATION
    // =====================================================================

    private String normalizeSchoolCode(
            String schoolCode
    ) {
        if (!StringUtils.hasText(schoolCode)) {
            throw new BadRequestException(
                    "School code is required to generate the Student identifier."
            );
        }

        String normalized =
                schoolCode
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z0-9]+")) {
            throw new BadRequestException(
                    "School code can contain only letters and numbers."
            );
        }

        return normalized;
    }

    private record BranchSequenceContext(
            Integer branchId,
            String schoolCode,
            String actor
    ) {
    }
}