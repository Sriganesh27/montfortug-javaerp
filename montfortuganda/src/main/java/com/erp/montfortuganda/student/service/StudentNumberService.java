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
 * Student Code:
 * {school_code}-STU-{academic_year_last_2_digits}-{4_digit_sequence}
 * Example: U021-STU-26-005
 *
 * Admission Number:
 * {school_code}-ADM-{6_digit_branch_lifetime_sequence}
 * Example: U021-ADM-00005
 *
 * Student-code sequences are maintained per branch and academic year.
 * Admission-number sequences are maintained for the lifetime of the branch
 * and do not reset when the academic year changes.
 */
@Service
public class StudentNumberService {

    private static final String STUDENT_MODULE_CODE =
            "STUDENT";

    private static final String ADMISSION_MODULE_CODE =
            "STUDENT_ADMISSION";

    private static final String STUDENT_CODE =
            "STU";

    private static final String ADMISSION_CODE =
            "ADM";

    /**
     * The admission-register sequence is branch-lifetime, so it does not
     * belong to a real academic year. Zero is the permanent sequence bucket.
     */
    private static final int ADMISSION_RUNNING_YEAR =
            0;

    private static final int STUDENT_SEQUENCE_PADDING =
            3;

    private static final int ADMISSION_SEQUENCE_PADDING =
            5;

    private static final long MAX_STUDENT_SEQUENCE =
            999L;

    private static final long MAX_ADMISSION_SEQUENCE =
            99_999L;

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
    @Transactional
    public String generateStudentCode(
            Branch branch,
            Long academicYearId,
            Integer authenticatedUserId
    ) {
        BranchSequenceContext context =
                requireBranchContext(
                        branch,
                        authenticatedUserId
                );

        int academicYear =
                resolveAcademicYear(
                        academicYearId,
                        context.branchId()
                );

        lockBranch(
                context.branchId()
        );

        long firstSequence =
                resolveExistingStudentCodeBaseline(
                        context.branchId(),
                        context.schoolCode(),
                        academicYear
                ) + 1L;

        long sequence =
                nextSequence(
                        context.branchId(),
                        STUDENT_MODULE_CODE,
                        academicYear,
                        context.actor(),
                        firstSequence
                );

        if (sequence > MAX_STUDENT_SEQUENCE) {
            throw new IllegalStateException(
                    "Student-code sequence exceeded "
                            + MAX_STUDENT_SEQUENCE
                            + " for school "
                            + context.schoolCode()
                            + " and academic year "
                            + academicYear
                            + "."
            );
        }

        int shortYear =
                Math.floorMod(
                        academicYear,
                        100
                );

        return String.format(
                Locale.ROOT,
                "%s-%s-%02d-%0"
                        + STUDENT_SEQUENCE_PADDING
                        + "d",
                context.schoolCode(),
                STUDENT_CODE,
                shortYear,
                sequence
        );
    }

    /**
     * Generates the next official branch-wide Admission Number.
     *
     * The sequence includes every Student ever admitted to the branch and
     * never resets by academic year.
     */
    @Transactional
    public String generateAdmissionNumber(
            Branch branch,
            Integer authenticatedUserId
    ) {
        BranchSequenceContext context =
                requireBranchContext(
                        branch,
                        authenticatedUserId
                );

        lockBranch(
                context.branchId()
        );

        long firstSequence =
                resolveExistingAdmissionBaseline(
                        context.branchId()
                ) + 1L;

        long sequence =
                nextSequence(
                        context.branchId(),
                        ADMISSION_MODULE_CODE,
                        ADMISSION_RUNNING_YEAR,
                        context.actor(),
                        firstSequence
                );

        if (sequence > MAX_ADMISSION_SEQUENCE) {
            throw new IllegalStateException(
                    "Admission-register sequence exceeded "
                            + MAX_ADMISSION_SEQUENCE
                            + " for school "
                            + context.schoolCode()
                            + "."
            );
        }

        return String.format(
                Locale.ROOT,
                "%s-%s-%0"
                        + ADMISSION_SEQUENCE_PADDING
                        + "d",
                context.schoolCode(),
                ADMISSION_CODE,
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
    // ACADEMIC YEAR
    // =====================================================================

    private int resolveAcademicYear(
            Long academicYearId,
            Integer branchId
    ) {
        if (academicYearId == null || academicYearId <= 0) {
            throw new BadRequestException(
                    "A valid academic year is required to generate the Student code."
            );
        }

        if (branchId == null || branchId <= 0) {
            throw new BadRequestException(
                    "A valid branch is required to validate the Academic Year."
            );
        }

        List<?> results =
                entityManager
                        .createNativeQuery(
                                """
                                select year(start_date)
                                from erp_academic_years
                                where academic_year_id = :academicYearId
                                  and branch_id = :branchId
                                  and active = 1
                                """
                        )
                        .setParameter(
                                "academicYearId",
                                academicYearId
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getResultList();

        if (results.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Selected Academic Year was not found, is inactive, "
                            + "or does not belong to this branch."
            );
        }

        Object result =
                results.getFirst();

        if (!(result instanceof Number number)) {
            throw new IllegalStateException(
                    "Academic year start year could not be resolved."
            );
        }

        int academicYear =
                number.intValue();

        if (academicYear < 1900 || academicYear > 2100) {
            throw new IllegalStateException(
                    "Academic year contains an unsupported start year."
            );
        }

        return academicYear;
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
     * already exist. The next generated Student code continues from the
     * highest matching code suffix.
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

        String prefix =
                String.format(
                        Locale.ROOT,
                        "%s-%s-%02d-%%",
                        schoolCode,
                        STUDENT_CODE,
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
                                                       student_code,
                                                       '-',
                                                       -1
                                                   ) as unsigned
                                               )
                                           ),
                                           0
                                       )
                                from erp_students
                                where branch_id = :branchId
                                  and student_code like :prefix
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .setParameter(
                                "prefix",
                                prefix
                        )
                        .getResultList();

        return firstNumberOrZero(
                results
        );
    }

    /**
     * Seeds the first branch-lifetime Admission Number safely for existing
     * installations. It considers both the total historical Student count
     * and the largest numeric suffix already present in admission_no.
     */
    private long resolveExistingAdmissionBaseline(
            Integer branchId
    ) {
        List<?> results =
                entityManager
                        .createNativeQuery(
                                """
                                select greatest(
                                           count(*),
                                           coalesce(
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
                                       )
                                from erp_students
                                where branch_id = :branchId
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
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