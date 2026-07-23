package com.erp.montfortuganda.employee.bulkimport.service;

import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.mapper.EmployeeMapper;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.employee.service.EmployeeAccountService;
import com.erp.montfortuganda.employee.service.EmployeeFileService;
import com.erp.montfortuganda.employee.service.EmployeeNumberService;
import com.erp.montfortuganda.employee.service.EmployeeServiceImpl;
import com.erp.montfortuganda.employee.service.EmployeeValidationService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Creates one Employee from a validated bulk-import row.
 *
 * <p>Every invocation runs in an independent transaction. Failure of one
 * Excel row therefore does not roll back Employees created from other rows.</p>
 *
 * <p>This service reuses the existing Employee validation, number generation,
 * mapping, private-file storage, login-account creation and welcome-email
 * event flow.</p>
 */
@Service
@RequiredArgsConstructor
public class EmployeeBulkImportTransactionService {

    private final BranchRepository branchRepository;
    private final EmployeeValidationService validationService;
    private final EmployeeNumberService numberService;
    private final EmployeeMapper employeeMapper;
    private final EmployeeFileService fileService;
    private final EmployeeAccountService accountService;
    private final ErpEmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates one Employee in a new transaction.
     *
     * @param request validated Employee registration request
     * @param branchId trusted branch ID stored against the import job
     * @param createdByUserId trusted authenticated user ID
     * @param createdByUsername authenticated username captured for the job
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class
    )
    public EmployeeBulkCreationResult createEmployee(
            EmployeeRegistrationRequest request,
            Integer branchId,
            Integer createdByUserId,
            String createdByUsername
    ) {
        Objects.requireNonNull(
                request,
                "Employee registration request is required."
        );

        validateContext(
                branchId,
                createdByUserId,
                createdByUsername
        );

        Branch branch =
                requireBranch(branchId);

        EmployeeValidationService.RegistrationReferences references =
                validationService.validateForBulkRegistration(
                        request,
                        branch,
                        createdByUserId,
                        createdByUsername
                );

        String employeeNo =
                numberService.generateEmployeeNumber(
                        branch,
                        request.employeeCategory(),
                        request.joiningDate()
                );

        ErpEmployee employee =
                employeeMapper.toNewEmployee(
                        request,
                        branch,
                        references.department(),
                        references.designation(),
                        references.reportingManager(),
                        employeeNo
                );

        /*
         * The default profile photo follows the same private storage,
         * MIME verification and rollback-cleanup flow as normal registration.
         */
        fileService.storeNewEmployeeFiles(
                request,
                employee
        );

        employee =
                employeeRepository.saveAndFlush(
                        employee
                );

        EmployeeAccountService.AccountCreationResult accountResult =
                accountService.createEmployeeAccount(
                        request.accountRequest(),
                        employee,
                        createdByUserId
                );

        /*
         * Account creation updates employee.user and loginEnabled.
         */
        employee =
                employeeRepository.saveAndFlush(
                        employee
                );

        publishWelcomeEmail(
                employee,
                accountResult
        );

        return new EmployeeBulkCreationResult(
                employee.getEmployeeId(),
                employee.getEmployeeNo(),
                employee.getFullName(),
                accountResult.created(),
                accountResult.userId(),
                accountResult.username(),
                accountResult.sendEmail(),
                resolveEmailStatus(accountResult)
        );
    }

    private Branch requireBranch(
            Integer branchId
    ) {
        Branch branch =
                branchRepository
                        .findById(branchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "The Employee import branch was not found."
                                )
                        );

        if (!Integer.valueOf(1).equals(branch.getIsActive())) {
            throw new BadRequestException(
                    "The Employee import branch is inactive."
            );
        }

        return branch;
    }

    private void validateContext(
            Integer branchId,
            Integer createdByUserId,
            String createdByUsername
    ) {
        if (branchId == null || branchId <= 0) {
            throw new BadRequestException(
                    "A valid import branch ID is required."
            );
        }

        if (createdByUserId == null || createdByUserId <= 0) {
            throw new BadRequestException(
                    "A valid import user ID is required."
            );
        }

        if (
                createdByUsername == null
                        || createdByUsername.isBlank()
        ) {
            throw new BadRequestException(
                    "The import username is required."
            );
        }
    }

    /**
     * Publishes the protected credential event inside the current transaction.
     *
     * <p>The existing TransactionalEventListener consumes it only after this
     * Employee transaction commits successfully.</p>
     */
    private void publishWelcomeEmail(
            ErpEmployee employee,
            EmployeeAccountService.AccountCreationResult accountResult
    ) {
        if (
                !accountResult.created()
                        || !accountResult.sendEmail()
        ) {
            return;
        }

        eventPublisher.publishEvent(
                new EmployeeServiceImpl.EmployeeWelcomeEmailRequestedEvent(
                        employee.getEmployeeId(),
                        accountResult.userId(),
                        accountResult.username(),
                        accountResult.temporaryPassword(),
                        false
                )
        );
    }

    private String resolveEmailStatus(
            EmployeeAccountService.AccountCreationResult accountResult
    ) {
        if (!accountResult.created()) {
            return "NOT_REQUIRED";
        }

        if (!accountResult.sendEmail()) {
            return "NOT_REQUESTED";
        }

        return "PENDING";
    }

    /**
     * Safe result returned to the bulk processor.
     *
     * <p>The temporary password is deliberately excluded.</p>
     */
    public record EmployeeBulkCreationResult(
            Long employeeId,
            String employeeNo,
            String employeeName,
            boolean accountCreated,
            Integer userId,
            String username,
            boolean emailRequested,
            String emailStatus
    ) {
    }
}