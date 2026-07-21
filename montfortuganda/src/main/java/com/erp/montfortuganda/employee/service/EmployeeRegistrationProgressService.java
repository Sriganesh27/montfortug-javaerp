package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.auth.entity.CredentialDeliveryStatus;
import com.erp.montfortuganda.employee.dto.response.EmployeeRegistrationResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeRegistrationResponse.RegistrationStage;
import com.erp.montfortuganda.employee.dto.response.EmployeeRegistrationResponse.RegistrationStatus;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores short-lived Employee registration progress for frontend polling.
 * <p>
 * Each operation is owned by the authenticated branch and user that created
 * it. Polling therefore requires both the operation ID and the current
 * security context values; knowing another operation UUID is not sufficient
 * to read its status.
 * <p>
 * This service stores no password, password hash, Base64 file data or private
 * file path. Completed and failed operations are retained temporarily so the
 * frontend can fetch the final result, then removed automatically during later
 * tracker operations.
 */
@SuppressWarnings("unused")
@Service
public class EmployeeRegistrationProgressService {

    private static final Duration PROCESSING_RETENTION =
            Duration.ofHours(2);

    private static final Duration FINAL_RESULT_RETENTION =
            Duration.ofHours(1);

    private static final int MAX_TRACKED_OPERATIONS =
            2_000;

    private static final int MAX_REPORTED_ERRORS =
            20;

    private static final int MAX_MESSAGE_LENGTH =
            1_000;

    private final Map<String, RegistrationOperation> operations =
            new ConcurrentHashMap<>();

    /**
     * Creates a branch- and user-owned registration operation.
     */
    public EmployeeRegistrationResponse createOperation(
            Integer branchId,
            Integer userId,
            String employeeDisplayName,
            Integer totalItems
    ) {
        requirePositiveId(
                branchId,
                "Authenticated branch ID"
        );

        requirePositiveId(
                userId,
                "Authenticated User ID"
        );

        cleanupExpiredOperations();

        if (operations.size() >= MAX_TRACKED_OPERATIONS) {
            throw new IllegalStateException(
                    "Too many Employee registration operations are currently tracked."
            );
        }

        String operationId =
                UUID.randomUUID()
                        .toString();

        LocalDateTime acceptedAt =
                nowUtc();

        RegistrationOperation operation =
                new RegistrationOperation(
                        operationId,
                        branchId,
                        userId,
                        trimToNull(employeeDisplayName),
                        normalizeTotalItems(totalItems),
                        acceptedAt
                );

        operations.put(
                operationId,
                operation
        );

        return operation.snapshot();
    }

    /**
     * Returns one operation only when it belongs to the authenticated branch
     * and user.
     */
    public EmployeeRegistrationResponse getStatus(
            String operationId,
            Integer branchId,
            Integer userId
    ) {
        RegistrationOperation operation =
                requireOwnedOperation(
                        operationId,
                        branchId,
                        userId
                );

        return operation.snapshot();
    }

    /**
     * Moves an operation to the specified processing stage.
     */
    public EmployeeRegistrationResponse updateStage(
            String operationId,
            Integer branchId,
            Integer userId,
            RegistrationStage stage,
            Integer percentage,
            String message
    ) {
        RegistrationOperation operation =
                requireOwnedOperation(
                        operationId,
                        branchId,
                        userId
                );

        operation.updateStage(
                stage,
                percentage,
                message
        );

        return operation.snapshot();
    }

    /**
     * Updates nested-record progress while keeping the current main stage.
     */
    public EmployeeRegistrationResponse updateItemProgress(
            String operationId,
            Integer branchId,
            Integer userId,
            Integer completedItems,
            Integer totalItems,
            String itemMessage
    ) {
        RegistrationOperation operation =
                requireOwnedOperation(
                        operationId,
                        branchId,
                        userId
                );

        operation.updateItemProgress(
                completedItems,
                totalItems,
                itemMessage
        );

        return operation.snapshot();
    }

    /**
     * Stores the successful final result. Plain temporary passwords must never
     * be included in RegistrationCompletion.
     */
    public EmployeeRegistrationResponse complete(
            String operationId,
            Integer branchId,
            Integer userId,
            RegistrationCompletion completion
    ) {
        Objects.requireNonNull(
                completion,
                "Employee registration completion details are required."
        );

        RegistrationOperation operation =
                requireOwnedOperation(
                        operationId,
                        branchId,
                        userId
                );

        operation.complete(completion);

        return operation.snapshot();
    }

    /**
     * Stores a safe failure result for frontend display.
     */
    public EmployeeRegistrationResponse fail(
            String operationId,
            Integer branchId,
            Integer userId,
            RegistrationStage stage,
            String message,
            List<String> errors
    ) {
        RegistrationOperation operation =
                requireOwnedOperation(
                        operationId,
                        branchId,
                        userId
                );

        operation.fail(
                stage,
                message,
                sanitizeErrors(errors)
        );

        return operation.snapshot();
    }

    /**
     * Convenience overload for one safe error message.
     */
    public EmployeeRegistrationResponse fail(
            String operationId,
            Integer branchId,
            Integer userId,
            RegistrationStage stage,
            String message,
            String error
    ) {
        return fail(
                operationId,
                branchId,
                userId,
                stage,
                message,
                StringUtils.hasText(error)
                        ? List.of(error.trim())
                        : List.of()
        );
    }

    /**
     * Removes a known operation after the caller no longer needs it.
     */
    public void removeOperation(
            String operationId,
            Integer branchId,
            Integer userId
    ) {
        RegistrationOperation operation =
                requireOwnedOperation(
                        operationId,
                        branchId,
                        userId
                );

        operations.remove(
                operation.operationId(),
                operation
        );
    }

    /**
     * Returns the current number of in-memory operations for diagnostics.
     */
    public int getTrackedOperationCount() {
        cleanupExpiredOperations();
        return operations.size();
    }

    private RegistrationOperation requireOwnedOperation(
            String operationId,
            Integer branchId,
            Integer userId
    ) {
        if (!StringUtils.hasText(operationId)) {
            throw new BadRequestException(
                    "Employee registration operation ID is required."
            );
        }

        requirePositiveId(
                branchId,
                "Authenticated branch ID"
        );

        requirePositiveId(
                userId,
                "Authenticated User ID"
        );

        cleanupExpiredOperations();

        RegistrationOperation operation =
                operations.get(
                        operationId.trim()
                );

        /*
         * Return the same not-found response for a missing operation and an
         * operation owned by another branch/user. This avoids ownership
         * disclosure.
         */
        if (
                operation == null
                        || !Objects.equals(
                        operation.branchId(),
                        branchId
                )
                        || !Objects.equals(
                        operation.userId(),
                        userId
                )
        ) {
            throw new ResourceNotFoundException(
                    "Employee registration operation was not found."
            );
        }

        return operation;
    }

    private void cleanupExpiredOperations() {
        LocalDateTime now =
                nowUtc();

        operations.entrySet()
                .removeIf(entry ->
                        entry.getValue()
                                .isExpired(now)
                );
    }

    private List<String> sanitizeErrors(
            List<String> errors
    ) {
        if (errors == null || errors.isEmpty()) {
            return List.of();
        }

        List<String> sanitized =
                new ArrayList<>();

        for (String error : errors) {
            if (!StringUtils.hasText(error)) {
                continue;
            }

            sanitized.add(
                    abbreviate(
                            error.trim()
                    )
            );

            if (sanitized.size() >= MAX_REPORTED_ERRORS) {
                break;
            }
        }

        return List.copyOf(sanitized);
    }

    private void requirePositiveId(
            Integer value,
            String label
    ) {
        if (value == null || value <= 0) {
            throw new BadRequestException(
                    label + " is required."
            );
        }
    }

    private int normalizeTotalItems(
            Integer totalItems
    ) {
        return totalItems == null
                ? 0
                : Math.max(
                totalItems,
                0
        );
    }

    private String trimToNull(
            String value
    ) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private String abbreviate(
            String value
    ) {
        if (value.length() <= MAX_MESSAGE_LENGTH) {
            return value;
        }

        return value.substring(
                0,
                MAX_MESSAGE_LENGTH
        );
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.now(
                ZoneOffset.UTC
        );
    }

    private String stageTitle(
            RegistrationStage stage
    ) {
        return switch (stage) {
            case REQUEST_ACCEPTED ->
                    "Request Accepted";
            case SERVER_VALIDATION ->
                    "Validating Employee";
            case EMPLOYEE_CREATION ->
                    "Creating Employee";
            case RELATED_RECORDS ->
                    "Saving Related Records";
            case REPORTING_MANAGER ->
                    "Assigning Reporting Manager";
            case LOGIN_ACCOUNT ->
                    "Creating Login Account";
            case FINALIZATION ->
                    "Finalizing Registration";
        };
    }

    /**
     * Safe successful result supplied by the Employee registration workflow.
     */
    public record RegistrationCompletion(
            Long employeeId,
            String employeeNo,
            String fullName,
            String departmentName,
            String designationName,
            String reportingManagerName,
            Boolean loginCreated,
            String loginAccountStatus,
            Integer userId,
            String username,
            String loginRole,
            CredentialDeliveryStatus credentialDeliveryStatus,
            Integer completedItems,
            Integer totalItems,
            String message
    ) {
    }

    /**
     * One mutable operation guarded by synchronized methods. The map itself is
     * concurrent, while synchronization keeps each multi-field snapshot
     * internally consistent.
     */
    private final class RegistrationOperation {

        private final String operationId;
        private final Integer branchId;
        private final Integer userId;
        private final String employeeDisplayName;
        private final LocalDateTime acceptedAt;

        private RegistrationStatus status;
        private RegistrationStage stage;
        private String stageTitle;
        private int percentage;
        private String message;

        private Long employeeId;
        private String employeeNo;
        private String fullName;
        private String departmentName;
        private String designationName;
        private String reportingManagerName;

        private Boolean loginCreated;
        private String loginAccountStatus;
        private Integer createdLoginUserId;
        private String username;
        private String loginRole;
        private CredentialDeliveryStatus credentialDeliveryStatus;

        private int completedItems;
        private int totalItems;
        private String itemMessage;
        private List<String> errors;

        private LocalDateTime lastUpdatedAt;
        private LocalDateTime completedAt;

        private RegistrationOperation(
                String operationId,
                Integer branchId,
                Integer userId,
                String employeeDisplayName,
                int totalItems,
                LocalDateTime acceptedAt
        ) {
            this.operationId = operationId;
            this.branchId = branchId;
            this.userId = userId;
            this.employeeDisplayName = employeeDisplayName;
            this.acceptedAt = acceptedAt;

            this.status = RegistrationStatus.PROCESSING;
            this.stage = RegistrationStage.REQUEST_ACCEPTED;
            this.stageTitle = stageTitle(this.stage);
            this.percentage = 5;
            this.message = "Employee registration request accepted.";

            this.loginCreated = false;
            this.credentialDeliveryStatus =
                    CredentialDeliveryStatus.NOT_REQUIRED;

            this.completedItems = 0;
            this.totalItems = totalItems;
            this.itemMessage = totalItems > 0
                    ? "Waiting to process related records."
                    : null;
            this.errors = List.of();

            this.lastUpdatedAt = acceptedAt;
        }

        private String operationId() {
            return operationId;
        }

        private Integer branchId() {
            return branchId;
        }

        private Integer userId() {
            return userId;
        }

        private synchronized void updateStage(
                RegistrationStage newStage,
                Integer newPercentage,
                String newMessage
        ) {
            ensureProcessing();

            this.stage =
                    Objects.requireNonNull(
                            newStage,
                            "Employee registration stage is required."
                    );

            this.stageTitle =
                    stageTitle(newStage);

            this.percentage =
                    normalizePercentage(
                            newPercentage
                    );

            this.message =
                    StringUtils.hasText(newMessage)
                            ? abbreviate(
                            newMessage.trim()
                    )
                            : this.message;

            touch();
        }

        private synchronized void updateItemProgress(
                Integer newCompletedItems,
                Integer newTotalItems,
                String newItemMessage
        ) {
            ensureProcessing();

            int normalizedTotal =
                    newTotalItems == null
                            ? totalItems
                            : Math.max(
                            newTotalItems,
                            0
                    );

            int normalizedCompleted =
                    newCompletedItems == null
                            ? completedItems
                            : Math.max(
                            newCompletedItems,
                            0
                    );

            this.totalItems =
                    normalizedTotal;

            this.completedItems =
                    normalizedTotal == 0
                            ? normalizedCompleted
                            : Math.min(
                            normalizedCompleted,
                            normalizedTotal
                    );

            this.itemMessage =
                    trimToNull(newItemMessage);

            touch();
        }

        private synchronized void complete(
                RegistrationCompletion completion
        ) {
            ensureProcessing();

            this.status =
                    RegistrationStatus.COMPLETED;
            this.stage =
                    RegistrationStage.FINALIZATION;
            this.stageTitle =
                    "Registration Completed";
            this.percentage = 100;

            this.employeeId =
                    completion.employeeId();
            this.employeeNo =
                    trimToNull(
                            completion.employeeNo()
                    );
            this.fullName =
                    trimToNull(
                            completion.fullName()
                    );
            this.departmentName =
                    trimToNull(
                            completion.departmentName()
                    );
            this.designationName =
                    trimToNull(
                            completion.designationName()
                    );
            this.reportingManagerName =
                    trimToNull(
                            completion.reportingManagerName()
                    );

            this.loginCreated =
                    Boolean.TRUE.equals(
                            completion.loginCreated()
                    );
            this.loginAccountStatus =
                    trimToNull(
                            completion.loginAccountStatus()
                    );
            this.createdLoginUserId =
                    completion.userId();
            this.username =
                    trimToNull(
                            completion.username()
                    );
            this.loginRole =
                    trimToNull(
                            completion.loginRole()
                    );
            this.credentialDeliveryStatus =
                    completion.credentialDeliveryStatus() == null
                            ? CredentialDeliveryStatus.NOT_REQUIRED
                            : completion.credentialDeliveryStatus();

            this.totalItems =
                    completion.totalItems() == null
                            ? totalItems
                            : Math.max(
                            completion.totalItems(),
                            0
                    );

            this.completedItems =
                    completion.completedItems() == null
                            ? this.totalItems
                            : Math.max(
                            completion.completedItems(),
                            0
                    );

            if (this.totalItems > 0) {
                this.completedItems =
                        Math.min(
                                this.completedItems,
                                this.totalItems
                        );
            }

            this.itemMessage =
                    "All related Employee records were processed.";

            this.message =
                    StringUtils.hasText(
                            completion.message()
                    )
                            ? abbreviate(
                            completion.message().trim()
                    )
                            : "Employee registration completed successfully.";

            this.errors = List.of();
            this.completedAt = nowUtc();

            touch();
        }

        private synchronized void fail(
                RegistrationStage failedStage,
                String failureMessage,
                List<String> failureErrors
        ) {
            if (status != RegistrationStatus.PROCESSING) {
                return;
            }

            this.status =
                    RegistrationStatus.FAILED;
            this.stage =
                    failedStage == null
                            ? stage
                            : failedStage;
            this.stageTitle =
                    "Registration Failed";
            this.percentage =
                    Math.min(
                            percentage,
                            99
                    );
            this.message =
                    StringUtils.hasText(
                            failureMessage
                    )
                            ? abbreviate(
                            failureMessage.trim()
                    )
                            : "Employee registration failed.";
            this.errors =
                    failureErrors == null
                            ? List.of()
                            : List.copyOf(
                            failureErrors
                    );
            this.completedAt = nowUtc();

            touch();
        }

        private synchronized EmployeeRegistrationResponse snapshot() {
            return new EmployeeRegistrationResponse(
                    operationId,
                    status,
                    stage,
                    stageTitle,
                    percentage,
                    message,
                    employeeId,
                    employeeNo,
                    fullName == null
                            ? employeeDisplayName
                            : fullName,
                    departmentName,
                    designationName,
                    reportingManagerName,
                    loginCreated,
                    loginAccountStatus,
                    createdLoginUserId,
                    username,
                    loginRole,
                    credentialDeliveryStatus,
                    completedItems,
                    totalItems,
                    itemMessage,
                    errors,
                    acceptedAt,
                    completedAt
            );
        }

        private synchronized boolean isExpired(
                LocalDateTime now
        ) {
            Duration retention =
                    status == RegistrationStatus.PROCESSING
                            ? PROCESSING_RETENTION
                            : FINAL_RESULT_RETENTION;

            return lastUpdatedAt
                    .plus(retention)
                    .isBefore(now);
        }

        private void ensureProcessing() {
            if (status != RegistrationStatus.PROCESSING) {
                throw new IllegalStateException(
                        "Employee registration operation is already finalized."
                );
            }
        }

        private int normalizePercentage(
                Integer value
        ) {
            if (value == null) {
                return percentage;
            }

            return Math.clamp(
                    value,
                    0,
                    99
            );
        }

        private void touch() {
            this.lastUpdatedAt =
                    nowUtc();
        }
    }
}
