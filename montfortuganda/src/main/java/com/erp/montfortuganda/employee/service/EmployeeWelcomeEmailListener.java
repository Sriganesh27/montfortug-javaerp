package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.notification.service.EmailService;
import com.erp.montfortuganda.school.entity.Branch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Sends Employee login credentials only after the Employee account
 * transaction commits successfully.
 *
 * <p>The listener runs asynchronously, loads a detached email-safe snapshot,
 * sends the branch-wise credential email, and records SENT, RESENT or FAILED
 * against the linked User account.</p>
 *
 * <p>The temporary password is never logged, persisted by this listener, or
 * returned through the Employee API.</p>
 */
@Component
public class EmployeeWelcomeEmailListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmployeeWelcomeEmailListener.class
            );

    private final ErpEmployeeRepository employeeRepository;
    private final EmployeeAccountService accountService;
    private final EmailService emailService;
    private final TransactionTemplate readOnlyTransaction;

    public EmployeeWelcomeEmailListener(
            ErpEmployeeRepository employeeRepository,
            EmployeeAccountService accountService,
            EmailService emailService,
            PlatformTransactionManager transactionManager
    ) {
        this.employeeRepository = employeeRepository;
        this.accountService = accountService;
        this.emailService = emailService;

        this.readOnlyTransaction =
                new TransactionTemplate(
                        transactionManager
                );

        this.readOnlyTransaction.setReadOnly(true);

        this.readOnlyTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
    }

    /**
     * Consumes the credential event after the Employee transaction commits.
     */
    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void sendEmployeeWelcomeEmail(
            EmployeeServiceImpl.EmployeeWelcomeEmailRequestedEvent event
    ) {
        if (!isValidEvent(event)) {
            LOGGER.error(
                    "Employee credential event was invalid and could not be processed."
            );
            return;
        }

        try {
            ErpEmployee emailSnapshot =
                    loadEmailSnapshot(event);

            emailService.sendEmployeeWelcomeEmail(
                    emailSnapshot,
                    event.username(),
                    event.temporaryPassword()
            );

            accountService.recordCredentialDeliverySuccess(
                    event.userId(),
                    event.resent()
            );

            LOGGER.info(
                    "Employee credential {} completed for Employee ID: {}",
                    event.resent() ? "resend" : "delivery",
                    event.employeeId()
            );
        } catch (RuntimeException exception) {
            recordFailureSafely(
                    event.userId()
            );

            LOGGER.error(
                    "Employee credential delivery failed for Employee ID: {}",
                    event.employeeId(),
                    exception
            );
        }
    }

    /**
     * Reads all fields needed by EmailService inside a short database
     * transaction and returns a detached object containing no lazy proxies.
     */
    private ErpEmployee loadEmailSnapshot(
            EmployeeServiceImpl.EmployeeWelcomeEmailRequestedEvent event
    ) {
        ErpEmployee snapshot =
                readOnlyTransaction.execute(
                        status -> {
                            ErpEmployee employee =
                                    employeeRepository
                                            .findById(
                                                    event.employeeId()
                                            )
                                            .orElseThrow(() ->
                                                    new IllegalStateException(
                                                            "Employee was not found for credential delivery."
                                                    )
                                            );

                            validateLinkedUser(
                                    employee,
                                    event.userId()
                            );

                            Branch branch =
                                    employee.getBranch();

                            if (branch == null) {
                                throw new IllegalStateException(
                                        "Employee branch was not found for credential delivery."
                                );
                            }

                            return createEmailSnapshot(
                                    employee,
                                    branch
                            );
                        }
                );

        return Objects.requireNonNull(
                snapshot,
                "Employee email snapshot could not be created."
        );
    }

    private void validateLinkedUser(
            ErpEmployee employee,
            Integer expectedUserId
    ) {
        User user =
                employee.getUser();

        if (
                user == null
                        || !Objects.equals(
                        user.getId(),
                        expectedUserId
                )
        ) {
            throw new IllegalStateException(
                    "Employee login account does not match the credential event."
            );
        }

        if (!Integer.valueOf(1).equals(user.getIsActive())) {
            throw new IllegalStateException(
                    "Employee login account is inactive."
            );
        }

        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            throw new IllegalStateException(
                    "Employee login account no longer requires temporary credentials."
            );
        }
    }

    /**
     * Copies only the Employee and Branch fields required by EmailService.
     */
    private ErpEmployee createEmailSnapshot(
            ErpEmployee employee,
            Branch branch
    ) {
        Branch branchSnapshot =
                new Branch();

        branchSnapshot.setBranchId(
                branch.getBranchId()
        );
        branchSnapshot.setBranchName(
                branch.getBranchName()
        );
        branchSnapshot.setSchoolCode(
                branch.getSchoolCode()
        );
        branchSnapshot.setBranchEmail(
                branch.getBranchEmail()
        );
        branchSnapshot.setEmailFromName(
                branch.getEmailFromName()
        );
        branchSnapshot.setEmailReplyTo(
                branch.getEmailReplyTo()
        );
        branchSnapshot.setEmailEnabled(
                branch.getEmailEnabled()
        );
        branchSnapshot.setBranchLogoUrl(
                branch.getBranchLogoUrl()
        );

        ErpEmployee employeeSnapshot =
                new ErpEmployee();

        employeeSnapshot.setEmployeeId(
                employee.getEmployeeId()
        );
        employeeSnapshot.setFirstName(
                employee.getFirstName()
        );
        employeeSnapshot.setLastName(
                employee.getLastName()
        );
        employeeSnapshot.setFullName(
                employee.getFullName()
        );
        employeeSnapshot.setOfficialEmail(
                employee.getOfficialEmail()
        );
        employeeSnapshot.setBranch(
                branchSnapshot
        );

        return employeeSnapshot;
    }

    private void recordFailureSafely(
            Integer userId
    ) {
        try {
            accountService.recordCredentialDeliveryFailure(
                    userId
            );
        } catch (RuntimeException statusException) {
            LOGGER.error(
                    "Could not record failed Employee credential delivery for User ID: {}",
                    userId,
                    statusException
            );
        }
    }

    private boolean isValidEvent(
            EmployeeServiceImpl.EmployeeWelcomeEmailRequestedEvent event
    ) {
        return event != null
                && event.employeeId() != null
                && event.employeeId() > 0
                && event.userId() != null
                && event.userId() > 0
                && StringUtils.hasText(
                event.username()
        )
                && StringUtils.hasText(
                event.temporaryPassword()
        );
    }
}
