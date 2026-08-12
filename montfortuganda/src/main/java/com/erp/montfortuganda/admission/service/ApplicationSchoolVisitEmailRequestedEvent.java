package com.erp.montfortuganda.admission.service;

import java.util.Objects;

/**
 * Internal after-commit request for delivering a School Visit scheduling email.
 *
 * <p>This event is used only when a School Visit is initially scheduled or
 * rescheduled. It must not be published when an employee is assigned or when
 * the visit is completed.</p>
 *
 * <p>The listener reloads the committed application before sending so the
 * email always uses the final persisted School Visit date/time.</p>
 */
public final class ApplicationSchoolVisitEmailRequestedEvent {

    private final Long applicationId;
    private final Action action;

    public ApplicationSchoolVisitEmailRequestedEvent(
            Long applicationId,
            Action action
    ) {
        this.applicationId =
                Objects.requireNonNull(
                        applicationId,
                        "Application ID is required."
                );

        if (applicationId <= 0) {
            throw new IllegalArgumentException(
                    "Application ID must be greater than zero."
            );
        }

        this.action =
                Objects.requireNonNull(
                        action,
                        "School Visit email action is required."
                );
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {
        SCHEDULED,
        RESCHEDULED
    }

    @Override
    public String toString() {
        return "ApplicationSchoolVisitEmailRequestedEvent{"
                + "applicationId="
                + applicationId
                + ", action="
                + action
                + '}';
    }
}
