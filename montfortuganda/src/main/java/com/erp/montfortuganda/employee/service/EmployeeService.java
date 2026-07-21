package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.employee.dto.request.EmployeeDeactivationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeLoginAccountRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeTemporaryPasswordRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeSearchRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.dto.response.EmployeeDetailResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeLoginRoleOptionResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeOptionResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeePageResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeRegistrationResponse;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * Employee module use cases exposed to the controller layer.
 */
@SuppressWarnings("unused")
public interface EmployeeService {

    /**
     * Registers an Employee and returns the operation result used by the
     * frontend progress workflow.
     */
    EmployeeRegistrationResponse registerEmployee(
            EmployeeRegistrationRequest request
    );

    /**
     * Returns the authenticated user's branch-owned registration status.
     */
    EmployeeRegistrationResponse getRegistrationStatus(
            String operationId
    );

    /**
     * Searches Employees within the authenticated user's branch.
     */
    EmployeePageResponse searchEmployees(
            EmployeeSearchRequest request,
            Integer page,
            Integer size,
            String sort
    );

    /**
     * Returns eligible active Employees from the authenticated branch for the
     * Reporting Manager dropdown.
     *
     * @param excludeEmployeeId optional Employee ID excluded during edit
     */
    List<EmployeeOptionResponse> getReportingManagers(
            Long excludeEmployeeId
    );

    /**
     * Returns active Employee-assignable login roles from the role master.
     */
    List<EmployeeLoginRoleOptionResponse> getLoginRoleOptions();

    /**
     * Returns one branch-owned Employee with all related records.
     */
    EmployeeDetailResponse getEmployee(
            Long employeeId
    );

    /**
     * Updates one branch-owned Employee and related records.
     */
    EmployeeDetailResponse updateEmployee(
            Long employeeId,
            EmployeeUpdateRequest request
    );

    /**
     * Creates a login account for an existing branch-owned Employee.
     */
    EmployeeDetailResponse createLoginAccount(
            Long employeeId,
            EmployeeLoginAccountRequest request
    );

    /**
     * Generates a new temporary password, invalidates the previous password
     * and sends the replacement credentials to the Employee email.
     */
    EmployeeDetailResponse resetAndSendTemporaryPassword(
            Long employeeId,
            EmployeeTemporaryPasswordRequest request
    );

    /**
     * Deactivates one Employee and their linked login account.
     */
    void deactivateEmployee(
            Long employeeId,
            EmployeeDeactivationRequest request
    );

    /**
     * Loads the Employee profile photo after branch ownership validation.
     */
    EmployeePrivateFile getProfilePhoto(
            Long employeeId
    );

    /**
     * Loads the Employee signature after branch ownership validation.
     */
    EmployeePrivateFile getSignature(
            Long employeeId
    );

    /**
     * Loads one qualification document after branch and Employee ownership
     * validation.
     */
    EmployeePrivateFile getQualificationFile(
            Long employeeId,
            Long qualificationId
    );

    /**
     * Loads one experience certificate after branch and Employee ownership
     * validation.
     */
    EmployeePrivateFile getExperienceCertificate(
            Long employeeId,
            Long experienceId
    );

    /**
     * Loads one relieving letter after branch and Employee ownership
     * validation.
     */
    EmployeePrivateFile getRelievingLetter(
            Long employeeId,
            Long experienceId
    );

    /**
     * Loads one general Employee document after branch and Employee ownership
     * validation.
     */
    EmployeePrivateFile getDocumentFile(
            Long employeeId,
            Long documentId
    );

    /**
     * Authorized private-file result returned to the controller.
     */
    record EmployeePrivateFile(
            Resource resource,
            String filename,
            String contentType,
            long contentLength
    ) {
    }
}
