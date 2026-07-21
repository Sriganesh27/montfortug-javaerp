package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.employee.entity.ErpEmployeeSequence;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.employee.repository.ErpEmployeeSequenceRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.school.entity.Branch;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Generates permanent branch/category/year Employee numbers.
 *
 * <p>Format:</p>
 *
 * <pre>
 * {SCHOOL_CODE}-{CATEGORY_CODE}-{YY}-{SEQUENCE}
 * </pre>
 *
 * <p>Examples:</p>
 *
 * <pre>
 * U011-T-26-001
 * U011-NT-26-002
 * </pre>
 *
 * <p>The sequence is stored in {@code erp_employee_sequences}. Existing
 * Employee rows are inspected when a sequence row is first created, so an
 * empty sequence table cannot restart numbering from 001 when Employees
 * already exist.</p>
 *
 * <p>Sequence advancement runs in an independent transaction. A failed
 * Employee registration may therefore leave a harmless number gap, but a
 * committed number will never be reused.</p>
 */
@SuppressWarnings("unused")
@Service
public class EmployeeNumberService {

    private static final int MAX_GENERATION_ATTEMPTS =
            3;

    private static final Pattern VALID_SCHOOL_CODE =
            Pattern.compile(
                    "[A-Z0-9]{2,10}"
            );

    private final ErpEmployeeSequenceRepository sequenceRepository;
    private final ErpEmployeeRepository employeeRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate sequenceTransaction;

    public EmployeeNumberService(
            ErpEmployeeSequenceRepository sequenceRepository,
            ErpEmployeeRepository employeeRepository,
            JdbcTemplate jdbcTemplate,
            PlatformTransactionManager transactionManager
    ) {
        this.sequenceRepository = sequenceRepository;
        this.employeeRepository = employeeRepository;
        this.jdbcTemplate = jdbcTemplate;

        this.sequenceTransaction =
                new TransactionTemplate(
                        transactionManager
                );

        this.sequenceTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        this.sequenceTransaction.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
    }

    /**
     * Generates and permanently reserves the next Employee number.
     *
     * @param branch authenticated Employee branch
     * @param category Employee category
     * @param joiningDate Employee joining date; its year controls the sequence
     * @return generated Employee number
     */
    public String generateEmployeeNumber(
            Branch branch,
            EmployeeCategory category,
            LocalDate joiningDate
    ) {
        Integer branchId =
                requireBranchId(branch);

        String schoolCode =
                normalizeSchoolCode(
                        branch.getSchoolCode()
                );

        EmployeeCategory employeeCategory =
                Objects.requireNonNull(
                        category,
                        "Employee category is required."
                );

        LocalDate effectiveJoiningDate =
                Objects.requireNonNull(
                        joiningDate,
                        "Employee joining date is required."
                );

        int sequenceYear =
                effectiveJoiningDate.getYear();

        String categoryCode =
                categoryCode(employeeCategory);

        String numberPrefix =
                buildNumberPrefix(
                        schoolCode,
                        categoryCode,
                        sequenceYear
                );

        RuntimeException lastFailure =
                null;

        for (
                int attempt = 1;
                attempt <= MAX_GENERATION_ATTEMPTS;
                attempt++
        ) {
            try {
                String generatedNumber =
                        sequenceTransaction.execute(
                                status ->
                                        generateInsideTransaction(
                                                branch,
                                                branchId,
                                                employeeCategory,
                                                sequenceYear,
                                                numberPrefix
                                        )
                        );

                return Objects.requireNonNull(
                        generatedNumber,
                        "Employee-number transaction returned no result."
                );
            } catch (
                    DataIntegrityViolationException
                            | TransientDataAccessException exception
            ) {
                lastFailure = exception;
            }
        }

        throw new IllegalStateException(
                "Could not generate a unique Employee number after "
                        + MAX_GENERATION_ATTEMPTS
                        + " attempts.",
                lastFailure
        );
    }

    private String generateInsideTransaction(
            Branch branch,
            Integer branchId,
            EmployeeCategory category,
            Integer sequenceYear,
            String numberPrefix
    ) {
        ErpEmployeeSequence sequence =
                sequenceRepository
                        .findForUpdate(
                                branchId,
                                category,
                                sequenceYear
                        )
                        .orElseGet(() ->
                                createInitialSequence(
                                        branch,
                                        branchId,
                                        category,
                                        sequenceYear,
                                        numberPrefix
                                )
                        );

        String candidate;

        do {
            ensureSequenceCapacity(sequence);

            int nextNumber =
                    sequence.nextNumber();

            candidate =
                    numberPrefix
                            + formatSequence(
                            nextNumber
                    );
        } while (
                employeeRepository
                        .existsByEmployeeNoIgnoreCase(
                                candidate
                        )
        );

        sequenceRepository.saveAndFlush(
                sequence
        );

        return candidate;
    }

    private ErpEmployeeSequence createInitialSequence(
            Branch branch,
            Integer branchId,
            EmployeeCategory category,
            Integer sequenceYear,
            String numberPrefix
    ) {
        int existingMaximum =
                findExistingMaximum(
                        branchId,
                        category,
                        numberPrefix
                );

        ErpEmployeeSequence sequence =
                new ErpEmployeeSequence(
                        branch,
                        category,
                        sequenceYear
                );

        sequence.setLastNumber(
                existingMaximum
        );

        /*
         * saveAndFlush makes the unique branch/category/year constraint fail
         * inside this independent transaction when another request created
         * the same row concurrently. The outer retry then loads and locks the
         * winning row.
         */
        return sequenceRepository
                .saveAndFlush(
                        sequence
                );
    }

    private int findExistingMaximum(
            Integer branchId,
            EmployeeCategory category,
            String numberPrefix
    ) {
        List<String> existingNumbers =
                jdbcTemplate.queryForList(
                        """
                                select employee_no
                                from erp_employees
                                where branch_id = ?
                                  and employee_category = ?
                                  and employee_no like ?
                                """,
                        String.class,
                        branchId,
                        category.name(),
                        numberPrefix + "%"
                );

        int maximum =
                0;

        for (String employeeNumber : existingNumbers) {
            int parsedNumber =
                    parseSequence(
                            employeeNumber,
                            numberPrefix
                    );

            maximum =
                    Math.max(
                            maximum,
                            parsedNumber
                    );
        }

        return maximum;
    }

    private int parseSequence(
            String employeeNumber,
            String numberPrefix
    ) {
        if (
                !StringUtils.hasText(employeeNumber)
                        || !employeeNumber.startsWith(numberPrefix)
        ) {
            return 0;
        }

        String sequencePart =
                employeeNumber.substring(
                        numberPrefix.length()
                );

        if (
                sequencePart.isBlank()
                        || !sequencePart
                        .chars()
                        .allMatch(
                                Character::isDigit
                        )
        ) {
            return 0;
        }

        try {
            return Integer.parseInt(
                    sequencePart
            );
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private Integer requireBranchId(
            Branch branch
    ) {
        if (
                branch == null
                        || branch.getBranchId() == null
                        || branch.getBranchId() <= 0
        ) {
            throw new BadRequestException(
                    "A saved Employee branch is required."
            );
        }

        return branch.getBranchId();
    }

    private String normalizeSchoolCode(
            String schoolCode
    ) {
        if (!StringUtils.hasText(schoolCode)) {
            throw new BadRequestException(
                    "The Employee branch has no school code."
            );
        }

        String normalized =
                schoolCode.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!VALID_SCHOOL_CODE.matcher(normalized).matches()) {
            throw new BadRequestException(
                    "School code must contain 2 to 10 uppercase letters "
                            + "or numbers."
            );
        }

        return normalized;
    }

    private String categoryCode(
            EmployeeCategory category
    ) {
        return switch (category) {
            case TEACHING ->
                    "T";
            case NON_TEACHING ->
                    "NT";
            case MANAGEMENT_TEACHING ->
                    "MT";
            case MANAGEMENT_NON_TEACHING ->
                    "MNT";
            case SUPPORT_STAFF ->
                    "SS";
        };
    }

    private String buildNumberPrefix(
            String schoolCode,
            String categoryCode,
            int year
    ) {
        return schoolCode
                + "-"
                + categoryCode
                + "-"
                + String.format(
                Locale.ROOT,
                "%02d",
                Math.floorMod(
                        year,
                        100
                )
        )
                + "-";
    }

    private String formatSequence(
            int sequenceNumber
    ) {
        return String.format(
                Locale.ROOT,
                "%03d",
                sequenceNumber
        );
    }

    private void ensureSequenceCapacity(
            ErpEmployeeSequence sequence
    ) {
        if (
                sequence.getLastNumber() != null
                        && sequence.getLastNumber()
                        == Integer.MAX_VALUE
        ) {
            throw new IllegalStateException(
                    "Employee-number sequence has reached its maximum value."
            );
        }
    }
}
