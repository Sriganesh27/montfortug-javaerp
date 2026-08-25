package com.erp.montfortuganda.scholarship.service;

import java.util.Objects;

/**
 * Internal after-commit request for delivering the public scholarship
 * application link.
 *
 * <p>The raw token exists only in memory. It must never be logged, included
 * in {@link #toString()}, or persisted. Only its SHA-256 hash is stored in
 * {@code erp_scholarship_applications}.</p>
 */
public final class ScholarshipApplicationLinkEmailRequestedEvent {

    private final Long scholarshipApplicationId;
    private final String rawToken;

    public ScholarshipApplicationLinkEmailRequestedEvent(
            Long scholarshipApplicationId,
            String rawToken
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

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Raw scholarship application token is required."
            );
        }

        this.rawToken = rawToken;
    }

    public Long getScholarshipApplicationId() {
        return scholarshipApplicationId;
    }

    public String getRawToken() {
        return rawToken;
    }

    @Override
    public String toString() {
        return "ScholarshipApplicationLinkEmailRequestedEvent{"
                + "scholarshipApplicationId="
                + scholarshipApplicationId
                + ", rawToken=[PROTECTED]"
                + '}';
    }
}
