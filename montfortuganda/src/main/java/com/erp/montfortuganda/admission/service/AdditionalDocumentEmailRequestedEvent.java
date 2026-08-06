package com.erp.montfortuganda.admission.service;

import java.util.Objects;

/**
 * Internal after-commit request for delivering an applicant's
 * additional-document upload link.
 *
 * <p>The raw token exists only in memory. It must never be logged, included
 * in {@link #toString()}, or persisted by the event listener.</p>
 */
public final class AdditionalDocumentEmailRequestedEvent {

    private final Long documentRequestId;

    private final String rawUploadToken;

    public AdditionalDocumentEmailRequestedEvent(
            Long documentRequestId,
            String rawUploadToken
    ) {
        this.documentRequestId =
                Objects.requireNonNull(
                        documentRequestId,
                        "Document request ID is required."
                );

        if (documentRequestId <= 0) {
            throw new IllegalArgumentException(
                    "Document request ID must be greater than zero."
            );
        }

        if (rawUploadToken == null
                || rawUploadToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Raw document upload token is required."
            );
        }

        this.rawUploadToken =
                rawUploadToken;
    }

    public Long getDocumentRequestId() {
        return documentRequestId;
    }

    public String getRawUploadToken() {
        return rawUploadToken;
    }

    @Override
    public String toString() {
        return "AdditionalDocumentEmailRequestedEvent{"
                + "documentRequestId="
                + documentRequestId
                + ", rawUploadToken=[PROTECTED]"
                + '}';
    }
}
