package com.erp.montfortuganda.student.bulkimport.processor;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;
import com.erp.montfortuganda.common.importframework.plugin.PluginProcessor;
import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import com.erp.montfortuganda.student.bulkimport.mapper.StudentBulkRequestMapper;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService;
import com.erp.montfortuganda.student.bulkimport.reference.StudentBulkReferenceService.StudentBulkReferenceData;
import com.erp.montfortuganda.student.bulkimport.service.StudentBulkImportTransactionService;
import com.erp.montfortuganda.student.dto.request.StudentCreateRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
            List<StudentBulkImportRow> validDtos,
            ImportContext context
    ) {
        long startedAt =
                System.currentTimeMillis();

        validateInput(
                validDtos,
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

        Set<Integer> targetRows =
                context.getTargetRowNumbers();

        int processed = 0;
        int succeeded = 0;
        int processingFailed = 0;

        List<ErpImportError> processingErrors =
                new ArrayList<>();

        for (StudentBulkImportRow row : validDtos) {
            if (
                    row == null
                            || row.isBlank()
                            || !shouldProcessRow(
                            row,
                            targetRows
                    )
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
            List<StudentBulkImportRow> validDtos,
            ImportContext context
    ) {
        if (validDtos == null) {
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
    // RETRY ROW FILTERING
    // =====================================================================

    private boolean shouldProcessRow(
            StudentBulkImportRow row,
            Set<Integer> targetRows
    ) {
        return targetRows == null
                || targetRows.isEmpty()
                || targetRows.contains(
                row.getExcelRowNumber()
        );
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
        return ErpImportError.builder()
                .jobId(
                        context.getJobId()
                )
                .rowNumber(
                        row.getExcelRowNumber()
                )
                .columnName(
                        "Student"
                )
                .cellValue(
                        limitToDatabaseLength(
                                buildStudentLabel(
                                        row
                                )
                        )
                )
                .errorCode(
                        resolveErrorCode(
                                exception
                        )
                )
                .severity(
                        "ERROR"
                )
                .message(
                        limitToDatabaseLength(
                                safeMessage(
                                        exception
                                )
                        )
                )
                .suggestedFix(
                        "Correct the Student row and retry this row."
                )
                .build();
    }

    private String resolveErrorCode(
            RuntimeException exception
    ) {
        String simpleName =
                exception.getClass()
                        .getSimpleName();

        String normalizedName =
                simpleName.toUpperCase(
                        Locale.ROOT
                );

        if (
                normalizedName.contains(
                        "DUPLICATE"
                )
                        || normalizedName.contains(
                        "CONSTRAINTVIOLATION"
                )
                        || normalizedName.contains(
                        "DATAINTEGRITY"
                )
        ) {
            return "STUDENT_DUPLICATE";
        }

        if (
                exception instanceof IllegalArgumentException
                        || normalizedName.contains(
                        "BADREQUEST"
                )
                        || normalizedName.contains(
                        "RESOURCENOTFOUND"
                )
                        || normalizedName.contains(
                        "VALIDATION"
                )
        ) {
            return "STUDENT_DATA_INVALID";
        }

        if (
                normalizedName.contains(
                        "OPTIMISTICLOCK"
                )
                        || normalizedName.contains(
                        "CONCURRENCY"
                )
                        || normalizedName.contains(
                        "CONFLICT"
                )
        ) {
            return "STUDENT_CONFLICT";
        }

        if (
                exception instanceof SecurityException
                        || normalizedName.contains(
                        "ACCESSDENIED"
                )
                        || normalizedName.contains(
                        "SECURITY"
                )
        ) {
            return "STUDENT_ACCESS_DENIED";
        }

        return "STUDENT_CREATE_FAILED";
    }

    // =====================================================================
    // SAFE ERROR MESSAGE
    // =====================================================================

    private String safeMessage(
            RuntimeException exception
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