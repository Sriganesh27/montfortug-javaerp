package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Delivers School Visit schedule/reschedule emails only after the related
 * admission transaction has committed successfully.
 */
@Component
public class ApplicationSchoolVisitEmailListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ApplicationSchoolVisitEmailListener.class
            );

    private final ErpApplicationRepository applicationRepository;
    private final EmailService emailService;

    public ApplicationSchoolVisitEmailListener(
            ErpApplicationRepository applicationRepository,
            EmailService emailService
    ) {
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            ApplicationSchoolVisitEmailRequestedEvent event
    ) {
        if (event == null) {
            return;
        }

        try {
            ErpApplication application =
                    applicationRepository
                            .findById(event.getApplicationId())
                            .orElse(null);

            if (application == null) {
                LOGGER.warn(
                        "School Visit email skipped because application {} was not found.",
                        event.getApplicationId()
                );
                return;
            }

            boolean rescheduled =
                    event.getAction()
                            == ApplicationSchoolVisitEmailRequestedEvent.Action.RESCHEDULED;

            emailService.sendSchoolVisitSchedule(
                    application,
                    rescheduled
            );
        } catch (Exception exception) {
            LOGGER.error(
                    "School Visit {} email failed for application {}.",
                    event.getAction(),
                    event.getApplicationId(),
                    exception
            );
        }
    }
}
