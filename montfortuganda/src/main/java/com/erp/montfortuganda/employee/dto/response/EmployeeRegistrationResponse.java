package com.erp.montfortuganda.employee.dto.response;

import com.erp.montfortuganda.auth.entity.CredentialDeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Employee registration progress and final-result response.

 * The same contract is returned when a registration request is accepted and
 * when its operation status is polled. Enum names intentionally match the
 * existing Employee frontend values.
 */
@SuppressWarnings("unused")
public record EmployeeRegistrationResponse(

        String operationId,

        RegistrationStatus status,

        RegistrationStage stage,

        String stageTitle,

        Integer percentage,

        String message,

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

        String itemMessage,

        List<String> errors,

        LocalDateTime acceptedAt,

        LocalDateTime completedAt
) {

    public EmployeeRegistrationResponse {
        errors = errors == null
                ? List.of()
                : List.copyOf(errors);
    }

    /**
     * Overall state of an Employee registration operation.
     */
    public enum RegistrationStatus {
        PROCESSING,
        COMPLETED,
        FAILED
    }

    /**
     * Ordered stages displayed by the existing registration progress modal.
     */
    public enum RegistrationStage {
        REQUEST_ACCEPTED,
        SERVER_VALIDATION,
        EMPLOYEE_CREATION,
        RELATED_RECORDS,
        REPORTING_MANAGER,
        LOGIN_ACCOUNT,
        FINALIZATION
    }
}