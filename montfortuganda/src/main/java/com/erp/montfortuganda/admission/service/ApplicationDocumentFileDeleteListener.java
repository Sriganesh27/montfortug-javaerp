package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Deletes the physical public-portal file only after the database transaction
 * that deactivated the application-document record has committed.
 *
 * <p>The application record is never deleted. The document row also remains
 * in the database as an inactive audit record.</p>
 */
@Component
public class ApplicationDocumentFileDeleteListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ApplicationDocumentFileDeleteListener.class
            );

    private final ErpApplicationDocumentRepository
            documentRepository;

    private final ApplicationPublicFileDeletionService
            fileDeletionService;

    public ApplicationDocumentFileDeleteListener(
            ErpApplicationDocumentRepository documentRepository,
            ApplicationPublicFileDeletionService fileDeletionService
    ) {
        this.documentRepository = documentRepository;
        this.fileDeletionService = fileDeletionService;
    }

    /**
     * Runs only after commit and reloads the authoritative document record.
     * A new read transaction prevents use of the already-completed mutation
     * transaction.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public void handleDocumentFileDeleteRequested(
            ApplicationDocumentFileDeleteRequestedEvent event
    ) {
        if (event == null) {
            return;
        }

        Long documentId =
                event.getDocumentId();

        ErpApplicationDocument document =
                documentRepository
                        .findById(documentId)
                        .orElse(null);

        if (document == null) {
            LOGGER.error(
                    "Physical application-document deletion was skipped "
                            + "because document {} was not found.",
                    documentId
            );
            return;
        }

        /*
         * Never delete a file while its document row is still active. This
         * check protects against an incorrectly published event or a rolled
         * back/changed database state.
         */
        if (!Boolean.FALSE.equals(
                document.getActive()
        )) {
            LOGGER.warn(
                    "Physical application-document deletion was skipped "
                            + "because document {} is still active.",
                    documentId
            );
            return;
        }

        String storedPath =
                document.getFilePath();

        try {
            boolean deleted =
                    fileDeletionService
                            .deleteApplicationDocument(
                                    storedPath
                            );

            if (deleted) {
                LOGGER.info(
                        "Physical public application file deleted "
                                + "for inactive document {}.",
                        documentId
                );
            } else {
                LOGGER.info(
                        "Physical public application file was already absent "
                                + "for inactive document {}.",
                        documentId
                );
            }
        } catch (Exception exception) {
            /*
             * The committed audit row must remain inactive even when the
             * filesystem operation fails. The failure is logged so operations
             * can safely retry cleanup without restoring or deleting the
             * application.
             */
            LOGGER.error(
                    "Physical public application file deletion failed "
                            + "for inactive document {}.",
                    documentId,
                    exception
            );
        }
    }
}
