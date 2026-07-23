package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.entity.CredentialDeliveryStatus;
import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.notification.service.EmailService;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.service.model.BranchAdminCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Sends Branch Admin credentials asynchronously after the branch/account
 * transaction commits, then records the delivery result in a new transaction.
 */
@Component
public class BranchAdminCredentialEmailListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    BranchAdminCredentialEmailListener.class
            );

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TransactionTemplate readOnlyTransaction;
    private final TransactionTemplate writeTransaction;

    public BranchAdminCredentialEmailListener(
            BranchRepository branchRepository,
            UserRepository userRepository,
            EmailService emailService,
            PlatformTransactionManager transactionManager
    ) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;

        this.readOnlyTransaction =
                new TransactionTemplate(
                        transactionManager
                );
        this.readOnlyTransaction.setReadOnly(true);
        this.readOnlyTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        this.writeTransaction =
                new TransactionTemplate(
                        transactionManager
                );
        this.writeTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void sendBranchAdminCredentials(
            BranchAdminCredentialEmailRequestedEvent event
    ) {
        if (event == null) {
            LOGGER.error(
                    "Branch Admin credential event was null."
            );
            return;
        }

        BranchAdminCredentials credentials =
                event.getCredentials();

        try {
            Branch branchSnapshot =
                    loadBranchEmailSnapshot(
                            event.getBranchId(),
                            credentials
                    );

            emailService.sendBranchAdminWelcomeEmail(
                    branchSnapshot,
                    credentials
            );

            recordDeliveryResult(
                    credentials,
                    event.isResent()
                            ? CredentialDeliveryStatus.RESENT
                            : CredentialDeliveryStatus.SENT,
                    true
            );

            LOGGER.info(
                    "Branch Admin credential {} completed for branch ID: {}",
                    event.isResent()
                            ? "resend"
                            : "delivery",
                    event.getBranchId()
            );
        } catch (RuntimeException exception) {
            recordFailureSafely(credentials);

            LOGGER.error(
                    "Branch Admin credentials email failed for branch ID: {}",
                    event.getBranchId(),
                    exception
            );
        }
    }

    private Branch loadBranchEmailSnapshot(
            Integer branchId,
            BranchAdminCredentials credentials
    ) {
        Branch snapshot =
                readOnlyTransaction.execute(status -> {
                    Branch branch =
                            branchRepository
                                    .findById(branchId)
                                    .orElseThrow(() ->
                                            new IllegalStateException(
                                                    "Branch was not found for credential delivery."
                                            )
                                    );

                    User user =
                            userRepository
                                    .findById(
                                            credentials.getUserId()
                                    )
                                    .orElseThrow(() ->
                                            new IllegalStateException(
                                                    "Branch Admin user was not found for credential delivery."
                                            )
                                    );

                    validateCurrentCredentials(
                            branch,
                            user,
                            credentials
                    );

                    return createEmailSnapshot(branch);
                });

        return Objects.requireNonNull(
                snapshot,
                "Branch email snapshot could not be created."
        );
    }

    private void validateCurrentCredentials(
            Branch branch,
            User user,
            BranchAdminCredentials credentials
    ) {
        if (
                user.getAssignedBranch() == null
                        || !Objects.equals(
                        user.getAssignedBranch()
                                .getBranchId(),
                        branch.getBranchId()
                )
        ) {
            throw new IllegalStateException(
                    "Branch Admin is not assigned to the credential branch."
            );
        }

        if (!Integer.valueOf(1).equals(user.getIsActive())) {
            throw new IllegalStateException(
                    "Branch Admin account is inactive."
            );
        }

        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            throw new IllegalStateException(
                    "Branch Admin account no longer requires temporary credentials."
            );
        }

        if (
                !Objects.equals(
                        user.getCredentialVersion(),
                        credentials.getCredentialVersion()
                )
        ) {
            throw new IllegalStateException(
                    "A newer Branch Admin credential has already been generated."
            );
        }
    }

    private Branch createEmailSnapshot(
            Branch branch
    ) {
        Branch snapshot = new Branch();

        snapshot.setBranchId(branch.getBranchId());
        snapshot.setBranchName(branch.getBranchName());
        snapshot.setSchoolCode(branch.getSchoolCode());
        snapshot.setBranchLocation(branch.getBranchLocation());
        snapshot.setBranchEmail(branch.getBranchEmail());
        snapshot.setEmailFromName(branch.getEmailFromName());
        snapshot.setEmailReplyTo(branch.getEmailReplyTo());
        snapshot.setEmailEnabled(branch.getEmailEnabled());

        return snapshot;
    }

    private void recordDeliveryResult(
            BranchAdminCredentials credentials,
            CredentialDeliveryStatus deliveryStatus,
            boolean sentSuccessfully
    ) {
        writeTransaction.executeWithoutResult(status -> {
            User user =
                    userRepository
                            .findById(
                                    credentials.getUserId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Branch Admin user was not found while recording credential delivery."
                                    )
                            );

            if (
                    !Objects.equals(
                            user.getCredentialVersion(),
                            credentials.getCredentialVersion()
                    )
            ) {
                LOGGER.warn(
                        "Skipped stale credential-delivery update for User ID: {}",
                        credentials.getUserId()
                );
                return;
            }

            Integer attempts =
                    user.getCredentialDeliveryAttempts();

            user.setCredentialDeliveryAttempts(
                    attempts == null
                            ? 1
                            : attempts + 1
            );
            user.setCredentialDeliveryStatus(
                    deliveryStatus
            );

            if (sentSuccessfully) {
                user.setCredentialsSentAt(
                        LocalDateTime.now(
                                ZoneOffset.UTC
                        )
                );
            }

            userRepository.save(user);
        });
    }

    private void recordFailureSafely(
            BranchAdminCredentials credentials
    ) {
        try {
            recordDeliveryResult(
                    credentials,
                    CredentialDeliveryStatus.FAILED,
                    false
            );
        } catch (RuntimeException statusException) {
            LOGGER.error(
                    "Could not record failed Branch Admin credential delivery for User ID: {}",
                    credentials.getUserId(),
                    statusException
            );
        }
    }
}
