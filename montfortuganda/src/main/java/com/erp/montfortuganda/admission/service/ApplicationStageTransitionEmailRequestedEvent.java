package com.erp.montfortuganda.admission.service;

import java.util.Objects;

/**
 * Internal after-commit request for delivering an applicant notification
 * created by an admission workflow transition.
 *
 * <p>The event carries only the saved history identifier. The listener must
 * load the committed history/application records again before sending and
 * must update email_status independently to SENT or FAILED.</p>
 */
public final class ApplicationStageTransitionEmailRequestedEvent {

    private final Long historyId;

    public ApplicationStageTransitionEmailRequestedEvent(
            Long historyId
    ) {
        this.historyId =
                Objects.requireNonNull(
                        historyId,
                        "Application workflow history ID is required."
                );

        if (historyId <= 0) {
            throw new IllegalArgumentException(
                    "Application workflow history ID must be greater than zero."
            );
        }
    }

    public Long getHistoryId() {
        return historyId;
    }

    @Override
    public String toString() {
        return "ApplicationStageTransitionEmailRequestedEvent{"
                + "historyId="
                + historyId
                + '}';
    }
}
