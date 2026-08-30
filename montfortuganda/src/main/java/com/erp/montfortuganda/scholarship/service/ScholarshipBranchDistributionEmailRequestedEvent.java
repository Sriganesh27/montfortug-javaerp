package com.erp.montfortuganda.scholarship.service;

import java.util.Objects;

/**
 * Internal request for the post-distribution Scholarship notification.
 *
 * <p>This event carries only the Scholarship Application identifier.
 * Recipient and Scholarship details are resolved server-side by the
 * after-commit listener.</p>
 */
public final class ScholarshipBranchDistributionEmailRequestedEvent {

    private final Long scholarshipApplicationId;

    public ScholarshipBranchDistributionEmailRequestedEvent(
            Long scholarshipApplicationId
    ) {
        this.scholarshipApplicationId =
                Objects.requireNonNull(
                        scholarshipApplicationId,
                        "Scholarship application ID is required."
                );

        if (scholarshipApplicationId <= 0L) {
            throw new IllegalArgumentException(
                    "Scholarship application ID must be greater than zero."
            );
        }
    }

    public Long getScholarshipApplicationId() {
        return scholarshipApplicationId;
    }

    @Override
    public String toString() {
        return "ScholarshipBranchDistributionEmailRequestedEvent{"
                + "scholarshipApplicationId="
                + scholarshipApplicationId
                + '}';
    }
}
