package com.erp.montfortuganda.admission.service;

import java.util.Objects;

/**
 * Internal after-commit request for deleting the physical file of an
 * application document that has already been deactivated in the database.
 *
 * <p>The event intentionally carries only the document identifier. The
 * listener must reload the committed document record, verify that it is
 * inactive, resolve the configured public-upload root safely, and then delete
 * only that document file.</p>
 */
public final class ApplicationDocumentFileDeleteRequestedEvent {

    private final Long documentId;

    public ApplicationDocumentFileDeleteRequestedEvent(
            Long documentId
    ) {
        this.documentId =
                Objects.requireNonNull(
                        documentId,
                        "Application document ID is required."
                );

        if (documentId <= 0) {
            throw new IllegalArgumentException(
                    "Application document ID must be greater than zero."
            );
        }
    }

    public Long getDocumentId() {
        return documentId;
    }

    @Override
    public String toString() {
        return "ApplicationDocumentFileDeleteRequestedEvent{"
                + "documentId="
                + documentId
                + '}';
    }
}
