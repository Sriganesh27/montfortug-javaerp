package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import com.erp.montfortuganda.admission.repository.ErpApplicationStatusHistoryRepository;
import com.erp.montfortuganda.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * Delivers one applicant workflow notification only after the transaction
 * that saved the application stage and history row has committed.
 *
 * <p>The listener reloads the committed history record, sends only the
 * applicant-safe workflow email and records the delivery result in a new
 * transaction.</p>
 */
@Component
public class ApplicationStageTransitionEmailListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ApplicationStageTransitionEmailListener.class
            );

    private static final String WORKFLOW_HISTORY_STAGE =
            "APPLICATION_WORKFLOW";

    private static final String WORKFLOW_EMAIL_TYPE =
            "APPLICATION_STAGE_TRANSITION";

    private static final String EMAIL_SENT =
            "SENT";

    private static final String EMAIL_FAILED =
            "FAILED";

    private final ErpApplicationStatusHistoryRepository
            historyRepository;

    private final EmailService emailService;

    public ApplicationStageTransitionEmailListener(
            ErpApplicationStatusHistoryRepository historyRepository,
            EmailService emailService
    ) {
        this.historyRepository = historyRepository;
        this.emailService = emailService;
    }

    /**
     * Uses a new transaction because the application workflow transaction has
     * already committed when this listener executes.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void handleApplicationStageTransitionEmailRequested(
            ApplicationStageTransitionEmailRequestedEvent event
    ) {
        if (event == null) {
            return;
        }

        Long historyId =
                event.getHistoryId();

        ErpApplicationStatusHistory history =
                historyRepository
                        .findById(historyId)
                        .orElse(null);

        if (history == null) {
            LOGGER.error(
                    "Application workflow email could not be sent "
                            + "because history {} was not found.",
                    historyId
            );
            return;
        }

        if (!Boolean.TRUE.equals(
                history.getActive()
        )
                || !Boolean.TRUE.equals(
                history.getEmailRequired()
        )) {
            return;
        }

        if (EMAIL_SENT.equalsIgnoreCase(
                history.getEmailStatus()
        )) {
            return;
        }

        if (!isWorkflowEmailHistory(history)) {
            markEmailFailed(history);

            LOGGER.error(
                    "Application workflow email was not sent for "
                            + "history {} because its workflow metadata "
                            + "was invalid.",
                    historyId
            );
            return;
        }

        try {
            emailService.sendApplicationStageTransition(
                    history.getApplication(),
                    history
            );

            LocalDateTime sentAt =
                    LocalDateTime.now();

            history.setEmailStatus(EMAIL_SENT);
            history.setEmailSentAt(sentAt);

            historyRepository.saveAndFlush(history);

            LOGGER.info(
                    "Application workflow email status marked SENT "
                            + "for history {}.",
                    historyId
            );
        } catch (Exception exception) {
            markEmailFailed(history);

            LOGGER.error(
                    "Application workflow email delivery failed "
                            + "for history {}.",
                    historyId,
                    exception
            );
        }
    }

    private boolean isWorkflowEmailHistory(
            ErpApplicationStatusHistory history
    ) {
        return hasText(history.getStage())
                && WORKFLOW_HISTORY_STAGE.equalsIgnoreCase(
                history.getStage().trim()
        )
                && hasText(history.getEmailType())
                && WORKFLOW_EMAIL_TYPE.equalsIgnoreCase(
                history.getEmailType().trim()
        )
                && hasText(history.getNewStatus())
                && history.getApplication() != null
                && history.getApplication()
                .getApplicationId() != null;
    }

    private void markEmailFailed(
            ErpApplicationStatusHistory history
    ) {
        history.setEmailStatus(EMAIL_FAILED);
        history.setEmailSentAt(null);

        historyRepository.saveAndFlush(history);
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}
