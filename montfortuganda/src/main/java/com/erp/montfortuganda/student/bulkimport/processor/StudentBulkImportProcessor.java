package com.erp.montfortuganda.student.bulkimport.processor;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;
import com.erp.montfortuganda.common.importframework.plugin.PluginProcessor;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import com.erp.montfortuganda.student.bulkimport.mapper.StudentBulkRequestMapper;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.StudentBulkReferenceData;
import com.erp.montfortuganda.student.bulkimport.service.StudentBulkImportTransactionService;
import com.erp.montfortuganda.student.dto.request.StudentCreateRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Processes validated Student bulk-import rows.
 *
 * <p>Each Student is created through a separate {@code REQUIRES_NEW}
 * transaction. A failed row therefore does not roll back Students created
 * from other valid rows.</p>
 *
 * <p>The processor does not accept branch ownership, Student Code,
 * Admission Number, status or audit values from Excel.</p>
 */
@Component
@RequiredArgsConstructor
public class StudentBulkImportProcessor
        implements PluginProcessor<StudentBulkImportRow> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    StudentBulkImportProcessor.class
            );

    private static final String REFERENCE_CACHE_KEY =
            "student.bulk.references";

    private static final int DATABASE_TEXT_MAX_LENGTH =
            1000;

    private static final int OPERATION_ID_MAX_LENGTH =
            100;

    private final StudentBulkReferenceService referenceService;
    private final StudentBulkRequestMapper requestMapper;

    private final StudentBulkImportTransactionService
            transactionService;

    @Override
    public ChunkProcessingResult processChunk(
            List<StudentBulkImportRow> validRows,
            ImportContext context
    ) {
        long startedAt =
                System.currentTimeMillis();

        validateInput(
                validRows,
                context
        );

        Integer branchId =
                parsePositiveInteger(
                        context.getBranchId(),
                        "Student import branch ID"
                );

        Integer userId =
                parsePositiveInteger(
                        context.getUserId(),
                        "Student import user ID"
                );

        StudentBulkReferenceData references =
                getReferences(
                        context,
                        branchId
                );

        int processed = 0;
        int succeeded = 0;
        int processingFailed = 0;

        List<ErpImportError> processingErrors =
                new ArrayList<>();

        /*
         * GenericExcelReader already enforces targetRowNumbers before rows
         * reach this processor. Re-filtering here is unsafe for compact
         * failed-only retry workbooks because the coordinator translates
         * compact row numbers back to original Excel row numbers.
         */
        for (StudentBulkImportRow row : validRows) {
            if (
                    row == null
                            || row.isBlank()
            ) {
                continue;
            }

            processed++;

            try {
                String operationId =
                        buildOperationId(
                                context,
                                row
                        );

                StudentCreateRequest request =
                        requestMapper.toCreateRequest(
                                row,
                                references,
                                operationId
                        );

                transactionService.createStudent(
                        request,
                        branchId,
                        userId
                );

                succeeded++;
            } catch (RuntimeException exception) {
                processingFailed++;

                processingErrors.add(
                        buildProcessingError(
                                row,
                                context,
                                exception
                        )
                );

                /*
                 * Do not log the complete Excel row or Student request.
                 * Student names, parent contacts and medical data may
                 * contain private information.
                 */
                LOGGER.warn(
                        "Student bulk import row failed. "
                                + "Job ID: {}, row: {}, exception: {}",
                        context.getJobId(),
                        row.getExcelRowNumber(),
                        exception.getClass()
                                .getSimpleName()
                );
            }
        }

        return ChunkProcessingResult.builder()
                .processed(processed)
                .succeeded(succeeded)
                .validationFailed(0)
                .processingFailed(processingFailed)
                .processingTimeMs(
                        System.currentTimeMillis()
                                - startedAt
                )
                .processingErrors(
                        List.copyOf(
                                processingErrors
                        )
                )
                .build();
    }

    // =====================================================================
    // INPUT VALIDATION
    // =====================================================================

    private void validateInput(
            List<StudentBulkImportRow> validRows,
            ImportContext context
    ) {
        if (validRows == null) {
            throw new IllegalArgumentException(
                    "Validated Student rows are required."
            );
        }

        if (context == null) {
            throw new IllegalArgumentException(
                    "Student import context is required."
            );
        }

        if (
                context.getJobId() == null
                        || context.getJobId()
                        .isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Student import job ID is required."
            );
        }

        if (context.getJobStateCache() == null) {
            throw new IllegalArgumentException(
                    "Student import job-state cache is required."
            );
        }
    }

    // =====================================================================
    // REFERENCE DATA
    // =====================================================================

    private StudentBulkReferenceData getReferences(
            ImportContext context,
            Integer branchId
    ) {
        Object cached =
                context.getJobStateCache()
                        .computeIfAbsent(
                                REFERENCE_CACHE_KEY,
                                ignored ->
                                        referenceService
                                                .loadReferences(
                                                        branchId
                                                )
                        );

        if (
                !(cached
                        instanceof StudentBulkReferenceData references)
        ) {
            throw new IllegalStateException(
                    "Student import reference cache is invalid."
            );
        }

        if (
                references.getBranchId() == null
                        || !branchId.equals(
                        references.getBranchId()
                )
        ) {
            throw new SecurityException(
                    "Student import branch reference mismatch."
            );
        }

        return references;
    }

    // =====================================================================
    // OPERATION ID
    // =====================================================================

    private String buildOperationId(
            ImportContext context,
            StudentBulkImportRow row
    ) {
        String operationId =
                context.getJobId()
                        .trim()
                        + "-ROW-"
                        + row.getExcelRowNumber();

        if (
                operationId.length()
                        <= OPERATION_ID_MAX_LENGTH
        ) {
            return operationId;
        }

        return operationId.substring(
                0,
                OPERATION_ID_MAX_LENGTH
        );
    }

    // =====================================================================
    // PROCESSING ERROR
    // =====================================================================

    private ErpImportError buildProcessingError(
            StudentBulkImportRow row,
            ImportContext context,
            RuntimeException exception
    ) {
        ProcessingFailureDetails details =
                resolveFailureDetails(
                        exception
                );

        return ErpImportError.builder()
                .jobId(
                        context.getJobId()
                )
                .rowNumber(
                        row.getExcelRowNumber()
                )
                .columnName(
                        details.columnName()
                )
                .cellValue(
                        limitToDatabaseLength(
                                buildStudentLabel(
                                        row
                                )
                        )
                )
                .errorCode(
                        details.errorCode()
                )
                .severity(
                        "ERROR"
                )
                .message(
                        limitToDatabaseLength(
                                details.message()
                        )
                )
                .suggestedFix(
                        limitToDatabaseLength(
                                details.suggestedFix()
                        )
                )
                .build();
    }

    /**
     * Converts internal exceptions into safe, exact and actionable row
     * errors without exposing SQL, package names or database internals.
     */
    private ProcessingFailureDetails resolveFailureDetails(
            RuntimeException exception
    ) {
        Throwable relevant =
                findRelevantCause(
                        exception
                );

        String message =
                safeMessage(
                        relevant
                );

        return switch (relevant) {
            case DuplicateResourceException ignored ->
                    new ProcessingFailureDetails(
                            "STUDENT_DUPLICATE",
                            resolveColumnFromMessage(message),
                            message,
                            "Review the duplicate detail, correct the identifying "
                                    + "Student value, or remove the row when that "
                                    + "Student is already registered."
                    );

            case ResourceNotFoundException ignored ->
                    new ProcessingFailureDetails(
                            "STUDENT_REFERENCE_NOT_FOUND",
                            resolveColumnFromMessage(message),
                            message,
                            "Verify that the referenced Academic Year, Education "
                                    + "Level, Class or Section is active for the "
                                    + "authenticated branch."
                    );

            case BadRequestException ignored ->
                    new ProcessingFailureDetails(
                            "STUDENT_DATA_INVALID",
                            resolveColumnFromMessage(message),
                            message,
                            suggestedFixForMessage(message)
                    );

            case IllegalArgumentException ignored ->
                    new ProcessingFailureDetails(
                            "STUDENT_DATA_INVALID",
                            resolveColumnFromMessage(message),
                            message,
                            suggestedFixForMessage(message)
                    );

            case DataIntegrityViolationException ignored ->
                    new ProcessingFailureDetails(
                            "STUDENT_DATABASE_CONSTRAINT",
                            "Student",
                            message,
                            "Review missing or duplicate Student values. Correct "
                                    + "the row and retry only this failed record."
                    );

            case ObjectOptimisticLockingFailureException ignored ->
                    new ProcessingFailureDetails(
                            "STUDENT_CONFLICT",
                            "Student",
                            "The Student record changed while this row was being processed.",
                            "Retry this failed row after refreshing the latest data."
                    );

            case SecurityException ignored ->
                    new ProcessingFailureDetails(
                            "STUDENT_ACCESS_DENIED",
                            "Branch",
                            message,
                            "Confirm that the logged-in user is authorized for the "
                                    + "selected branch and retry the import."
                    );

            default ->
                    new ProcessingFailureDetails(
                            "STUDENT_CREATE_FAILED",
                            resolveColumnFromMessage(message),
                            message,
                            "Correct the value described in the error. When the message "
                                    + "does not identify a field, review the server log "
                                    + "using the job ID and Excel row number."
                    );
        };
    }

    /**
     * Finds the most useful safe cause while retaining the public business
     * exception whenever one exists in the chain.
     */
    private Throwable findRelevantCause(
            Throwable throwable
    ) {
        if (throwable == null) {
            throw new IllegalArgumentException(
                    "Student processing exception is required."
            );
        }

        Throwable current =
                throwable;

        Throwable deepest =
                throwable;

        int depth = 0;

        while (
                current != null
                        && depth < 12
        ) {
            if (
                    current instanceof DuplicateResourceException
                            || current instanceof ResourceNotFoundException
                            || current instanceof BadRequestException
                            || current instanceof IllegalArgumentException
                            || current instanceof DataIntegrityViolationException
                            || current instanceof ObjectOptimisticLockingFailureException
                            || current instanceof SecurityException
            ) {
                return current;
            }

            deepest = current;
            current = current.getCause();
            depth++;
        }

        return deepest;
    }

    private String resolveColumnFromMessage(
            String message
    ) {
        String normalized =
                message == null
                        ? ""
                        : message.toLowerCase(
                        Locale.ROOT
                );

        if (normalized.contains("academic year")) {
            return "Academic Year";
        }

        if (
                normalized.contains("education level")
                        || normalized.contains("level")
        ) {
            return "Education Level";
        }

        if (normalized.contains("section")) {
            return "Section";
        }

        if (normalized.contains("class")) {
            return "Class";
        }

        if (
                normalized.contains("date of birth")
                        || normalized.contains("birth date")
        ) {
            return "Date of Birth";
        }

        if (normalized.contains("gender")) {
            return "Gender";
        }

        if (
                normalized.contains("mobile")
                        || normalized.contains("phone")
        ) {
            return "Mobile No";
        }

        if (normalized.contains("email")) {
            return "Email";
        }

        if (
                normalized.contains("parent")
                        || normalized.contains("guardian")
                        || normalized.contains("preferred contact")
                        || normalized.contains("fee responsibility")
        ) {
            return "Father/Guardian Name";
        }

        if (
                normalized.contains("learner")
                        || normalized.contains("lin")
        ) {
            return "National ID/Passport";
        }

        if (normalized.contains("roll")) {
            return "Class";
        }

        if (
                normalized.contains("admission")
                        && normalized.contains("year")
        ) {
            return "Admission Year";
        }

        if (
                normalized.contains("first name")
                        || normalized.contains("student name")
        ) {
            return "First Name";
        }

        if (normalized.contains("branch")) {
            return "Branch";
        }

        return "Student";
    }

    private String suggestedFixForMessage(
            String message
    ) {
        String column =
                resolveColumnFromMessage(
                        message
                );

        return switch (column) {
            case "Academic Year" ->
                    "Use an active or planned Academic Year configured for the branch.";

            case "Education Level" ->
                    "Use Nursery, Primary, Secondary or Senior Secondary, or leave it blank when Class can determine the level.";

            case "Class" ->
                    "Use N1-N3, P1-P7, S1-S4 or S5-S6 and ensure the class is configured for the branch.";

            case "Section" ->
                    "Use an active Section belonging to the selected Academic Year and Class, or leave Section blank.";

            case "Date of Birth" ->
                    "Use a valid date such as 2017-07-31, 7/31/2017 or 31/07/2017, or leave the cell blank.";

            case "Gender" ->
                    "Use a supported Gender value or leave the cell blank.";

            case "Mobile No" ->
                    "Enter a valid mobile number or leave the cell blank.";

            case "Email" ->
                    "Enter a valid email address or leave the cell blank.";

            case "Admission Year" ->
                    "Enter a four-digit Admission Year or leave it blank to use the current year.";

            default ->
                    "Correct the value described in the error and retry only this failed row.";
        };
    }

    private record ProcessingFailureDetails(
            String errorCode,
            String columnName,
            String message,
            String suggestedFix
    ) {
    }

    // =====================================================================
    // SAFE ERROR MESSAGE
    // =====================================================================

    private String safeMessage(
            Throwable exception
    ) {
        String message =
                exception.getMessage();

        if (
                message == null
                        || message.isBlank()
        ) {
            return "Student creation failed due to an internal processing error.";
        }

        String normalizedMessage =
                message.toLowerCase(
                        Locale.ROOT
                );

        if (
                containsSensitiveTechnicalInformation(
                        normalizedMessage
                )
        ) {
            return "Student creation failed due to a database "
                    + "or internal processing error.";
        }

        return message.trim();
    }

    @SuppressWarnings("SpellCheckingInspection")
    private boolean containsSensitiveTechnicalInformation(
            String message
    ) {
        return message.contains(
                "select "
        )
                || message.contains(
                "insert "
        )
                || message.contains(
                "update "
        )
                || message.contains(
                "delete "
        )
                || message.contains(
                "jdbc:"
        )
                || message.contains(
                "hibernate"
        )
                || message.contains(
                "sqlstate"
        )
                || message.contains(
                "constraint ["
        )
                || message.contains(
                "java.io"
        )
                || message.contains(
                "java.nio"
        )
                || message.contains(
                "com.erp."
        )
                || message.contains(
                "org.springframework"
        )
                || message.contains(
                "jakarta.persistence"
        );
    }

    // =====================================================================
    // STUDENT LABEL
    // =====================================================================

    private String buildStudentLabel(
            StudentBulkImportRow row
    ) {
        String firstName =
                safeText(
                        row.getFirstName()
                );

        String middleName =
                safeText(
                        row.getMiddleName()
                );

        String lastName =
                safeText(
                        row.getLastName()
                );

        String fullName =
                String.join(
                                " ",
                                firstName,
                                middleName,
                                lastName
                        )
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        if (!fullName.isBlank()) {
            return fullName;
        }

        return "Excel row "
                + row.getExcelRowNumber();
    }

    private String safeText(
            String value
    ) {
        return value == null
                ? ""
                : value.trim();
    }

    // =====================================================================
    // CONTEXT VALUE PARSING
    // =====================================================================

    private Integer parsePositiveInteger(
            String value,
            String label
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    label + " is required."
            );
        }

        try {
            int parsed =
                    Integer.parseInt(
                            value.trim()
                    );

            if (parsed <= 0) {
                throw new NumberFormatException();
            }

            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    label + " is invalid."
            );
        }
    }

    // =====================================================================
    // DATABASE LENGTH SAFETY
    // =====================================================================

    private String limitToDatabaseLength(
            String value
    ) {
        if (value == null) {
            return null;
        }

        if (
                value.length()
                        <= DATABASE_TEXT_MAX_LENGTH
        ) {
            return value;
        }

        return value.substring(
                0,
                DATABASE_TEXT_MAX_LENGTH
        );
    }
}