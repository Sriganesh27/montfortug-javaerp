package com.erp.montfortuganda.employee.bulkimport.processor;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;
import com.erp.montfortuganda.common.importframework.plugin.PluginProcessor;
import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportOptions;
import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportRow;
import com.erp.montfortuganda.employee.bulkimport.mapper.EmployeeBulkRequestMapper;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkImportTransactionService;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkReferenceService;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkReferenceService.EmployeeBulkReferenceData;
import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Processes validated Employee bulk-import rows.
 *
 * <p>Each Employee is created through a separate REQUIRES_NEW transaction.
 * Therefore, one failed row does not roll back Employees created from
 * successful rows.</p>
 *
 * <p>This processor never handles or logs temporary passwords.</p>
 */
@Component
@RequiredArgsConstructor
public class EmployeeBulkImportProcessor
        implements PluginProcessor<EmployeeBulkImportRow> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmployeeBulkImportProcessor.class
            );

    private static final String REFERENCE_CACHE_KEY =
            "employee.bulk.references";

    private static final int DATABASE_TEXT_MAX_LENGTH =
            1000;

    /**
     * Import option names populated by the Employee bulk-import job creator.
     */
    public static final String OPTION_CREATE_CREDENTIALS =
            "createCredentials";

    public static final String OPTION_SEND_EMAIL =
            "sendEmail";

    public static final String OPTION_ROLE_ID =
            "roleId";

    public static final String OPTION_SUBMITTED_BY_USERNAME =
            "submittedByUsername";

    private final EmployeeBulkReferenceService referenceService;
    private final EmployeeBulkRequestMapper requestMapper;
    private final EmployeeBulkImportTransactionService transactionService;

    @Override
    public ChunkProcessingResult processChunk(
            List<EmployeeBulkImportRow> validDtos,
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
                        "Employee import branch ID"
                );

        Integer userId =
                parsePositiveInteger(
                        context.getUserId(),
                        "Employee import user ID"
                );

        EmployeeBulkImportOptions options =
                readOptions(context);

        String submittedByUsername =
                readRequiredUsername(context);

        EmployeeBulkReferenceData references =
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

        for (EmployeeBulkImportRow row : validDtos) {
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
                EmployeeRegistrationRequest request =
                        requestMapper.toRegistrationRequest(
                                row,
                                options,
                                references
                        );

                transactionService.createEmployee(
                        request,
                        branchId,
                        userId,
                        submittedByUsername
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
                 * Do not log the complete row, registration request,
                 * account result or temporary password.
                 */
                LOGGER.warn(
                        "Employee bulk import row failed. "
                                + "Job ID: {}, row: {}, exception: {}",
                        context.getJobId(),
                        row.getExcelRowNumber(),
                        exception.getClass().getSimpleName()
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
                        List.copyOf(processingErrors)
                )
                .build();
    }

    private void validateInput(
            List<EmployeeBulkImportRow> validDtos,
            ImportContext context
    ) {
        if (validDtos == null) {
            throw new IllegalArgumentException(
                    "Validated Employee rows are required."
            );
        }

        if (context == null) {
            throw new IllegalArgumentException(
                    "Employee import context is required."
            );
        }

        if (
                context.getJobId() == null
                        || context.getJobId().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Employee import job ID is required."
            );
        }

        if (context.getImportOptions() == null) {
            throw new IllegalArgumentException(
                    "Employee import options are required."
            );
        }

        if (context.getJobStateCache() == null) {
            throw new IllegalArgumentException(
                    "Employee import job-state cache is required."
            );
        }
    }

    private EmployeeBulkImportOptions readOptions(
            ImportContext context
    ) {
        Map<String, Object> values =
                context.getImportOptions();

        boolean createCredentials =
                readBoolean(
                        values.get(
                                OPTION_CREATE_CREDENTIALS
                        )
                );

        boolean sendEmail =
                readBoolean(
                        values.get(
                                OPTION_SEND_EMAIL
                        )
                );

        Long roleId =
                readOptionalLong(
                        values.get(
                                OPTION_ROLE_ID
                        )
                );

        EmployeeBulkImportOptions options =
                EmployeeBulkImportOptions.builder()
                        .createCredentials(
                                createCredentials
                        )
                        .sendEmail(
                                sendEmail
                        )
                        .roleId(roleId)
                        .build();

        options.validate();

        if (
                createCredentials
                        && roleId == null
        ) {
            throw new IllegalArgumentException(
                    "A login role is required when "
                            + "Create Credentials is enabled."
            );
        }

        return options;
    }

    private String readRequiredUsername(
            ImportContext context
    ) {
        Object value =
                context.getImportOptions()
                        .get(
                                OPTION_SUBMITTED_BY_USERNAME
                        );

        if (
                value == null
                        || value.toString().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Employee import submitting username is required."
            );
        }

        return value.toString()
                .trim();
    }

    private EmployeeBulkReferenceData getReferences(
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

        if (!(cached instanceof EmployeeBulkReferenceData references)) {
            throw new IllegalStateException(
                    "Employee import reference cache is invalid."
            );
        }

        if (
                references.getBranchId() == null
                        || !branchId.equals(
                        references.getBranchId()
                )
        ) {
            throw new SecurityException(
                    "Employee import branch reference mismatch."
            );
        }

        return references;
    }

    private boolean shouldProcessRow(
            EmployeeBulkImportRow row,
            Set<Integer> targetRows
    ) {
        return targetRows == null
                || targetRows.isEmpty()
                || targetRows.contains(
                row.getExcelRowNumber()
        );
    }

    private ErpImportError buildProcessingError(
            EmployeeBulkImportRow row,
            ImportContext context,
            RuntimeException exception
    ) {
        String safeMessage =
                safeMessage(exception);

        return ErpImportError.builder()
                .jobId(
                        context.getJobId()
                )
                .rowNumber(
                        row.getExcelRowNumber()
                )
                .columnName(
                        "Employee"
                )
                .cellValue(
                        limitToDatabaseLength(
                                buildEmployeeLabel(row)
                        )
                )
                .errorCode(
                        resolveErrorCode(exception)
                )
                .severity(
                        "ERROR"
                )
                .message(
                        limitToDatabaseLength(
                                safeMessage
                        )
                )
                .suggestedFix(
                        "Correct the Employee row and retry this row."
                )
                .build();
    }

    private String resolveErrorCode(
            RuntimeException exception
    ) {
        String simpleName =
                exception.getClass()
                        .getSimpleName();

        if (
                simpleName.contains("Duplicate")
                        || simpleName.contains(
                        "ConstraintViolation"
                )
                        || simpleName.contains(
                        "DataIntegrity"
                )
        ) {
            return "EMPLOYEE_DUPLICATE";
        }

        if (
                exception instanceof IllegalArgumentException
                        || simpleName.contains(
                        "BadRequest"
                )
                        || simpleName.contains(
                        "ResourceNotFound"
                )
        ) {
            return "EMPLOYEE_DATA_INVALID";
        }

        if (
                simpleName.contains(
                        "OptimisticLock"
                )
                        || simpleName.contains(
                        "Concurrency"
                )
        ) {
            return "EMPLOYEE_CONFLICT";
        }

        return "EMPLOYEE_CREATE_FAILED";
    }

    private String safeMessage(
            RuntimeException exception
    ) {
        String message =
                exception.getMessage();

        if (
                message == null
                        || message.isBlank()
        ) {
            return "Employee creation failed due to an internal processing error.";
        }

        /*
         * Do not expose SQL statements, JDBC URLs, Hibernate details,
         * filesystem paths or Java implementation information.
         */
        String lower =
                message.toLowerCase();

        if (
                lower.contains("select ")
                        || lower.contains("insert ")
                        || lower.contains("update ")
                        || lower.contains("delete ")
                        || lower.contains("jdbc:")
                        || lower.contains("hibernate")
                        || lower.contains("java.io")
                        || lower.contains("java.nio")
        ) {
            return "Employee creation failed due to a database or file-processing error.";
        }

        return message.trim();
    }

    private String buildEmployeeLabel(
            EmployeeBulkImportRow row
    ) {
        String firstName =
                safeText(
                        row.getFirstName()
                );

        String lastName =
                safeText(
                        row.getLastName()
                );

        String fullName =
                (firstName + " " + lastName)
                        .trim();

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

    private boolean readBoolean(
            Object value
    ) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        String text =
                value.toString()
                        .trim();

        if (
                "true".equalsIgnoreCase(text)
                        || "yes".equalsIgnoreCase(text)
                        || "1".equals(text)
        ) {
            return true;
        }

        if (
                "false".equalsIgnoreCase(text)
                        || "no".equalsIgnoreCase(text)
                        || "0".equals(text)
        ) {
            return false;
        }

        throw new IllegalArgumentException(
                "Employee import boolean option is invalid."
        );
    }

    private Long readOptionalLong(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            long parsed =
                    number.longValue();

            if (parsed <= 0) {
                throw new IllegalArgumentException(
                        "Employee login role ID is invalid."
                );
            }

            return parsed;
        }

        String text =
                value.toString()
                        .trim();

        if (text.isBlank()) {
            return null;
        }

        try {
            long parsed =
                    Long.parseLong(text);

            if (parsed <= 0) {
                throw new NumberFormatException();
            }

            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Employee login role ID is invalid."
            );
        }
    }

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