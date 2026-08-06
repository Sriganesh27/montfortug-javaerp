package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRequestRepository;
import com.erp.montfortuganda.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Sends an additional-document email only after the database transaction that
 * created the request has committed successfully.
 *
 * <p>The raw upload token is used only in memory. Before sending, its SHA-256
 * hash is compared with the hash stored for the request. The raw token is
 * never logged or persisted.</p>
 */
@Component
public class AdditionalDocumentEmailListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AdditionalDocumentEmailListener.class
            );

    private final ErpApplicationDocumentRequestRepository
            documentRequestRepository;

    private final EmailService emailService;

    public AdditionalDocumentEmailListener(
            ErpApplicationDocumentRequestRepository
                    documentRequestRepository,
            EmailService emailService
    ) {
        this.documentRequestRepository =
                documentRequestRepository;
        this.emailService = emailService;
    }

    /**
     * Uses a new transaction because the document-request creation transaction
     * has already committed when this listener runs.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void handleAdditionalDocumentEmailRequested(
            AdditionalDocumentEmailRequestedEvent event
    ) {
        if (event == null) {
            return;
        }

        Long requestId =
                event.getDocumentRequestId();

        ErpApplicationDocumentRequest request =
                documentRequestRepository
                        .findById(requestId)
                        .orElse(null);

        if (request == null) {
            LOGGER.error(
                    "Additional-document email could not be sent "
                            + "because request {} was not found.",
                    requestId
            );
            return;
        }

        if (!Boolean.TRUE.equals(request.getActive())
                || !Boolean.TRUE.equals(
                        request.getEmailRequired()
                )) {
            return;
        }

        if (request.getEmailStatus()
                == ErpApplicationDocumentRequest
                .EmailStatus.SENT) {
            return;
        }

        if (request.getRequestStatus()
                != ErpApplicationDocumentRequest
                .RequestStatus.PENDING) {
            markEmailFailed(request);

            LOGGER.warn(
                    "Additional-document email was not sent for "
                            + "request {} because its status is {}.",
                    requestId,
                    request.getRequestStatus()
            );
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (isExpired(request, now)) {
            request.setRequestStatus(
                    ErpApplicationDocumentRequest
                            .RequestStatus.EXPIRED
            );
            markEmailFailed(request);

            LOGGER.warn(
                    "Additional-document email was not sent for "
                            + "request {} because its upload link "
                            + "had already expired.",
                    requestId
            );
            return;
        }

        if (!tokenMatches(
                event.getRawUploadToken(),
                request.getUploadTokenHash()
        )) {
            markEmailFailed(request);

            LOGGER.error(
                    "Additional-document email was not sent for "
                            + "request {} because token validation failed.",
                    requestId
            );
            return;
        }

        try {
            emailService.sendAdditionalDocumentRequest(
                    request.getApplication(),
                    request,
                    event.getRawUploadToken()
            );

            request.setEmailStatus(
                    ErpApplicationDocumentRequest
                            .EmailStatus.SENT
            );
            request.setEmailSentAt(now);
            request.setUpdatedAt(now);

            documentRequestRepository.save(request);

            LOGGER.info(
                    "Additional-document email status marked SENT "
                            + "for request {}.",
                    requestId
            );
        } catch (Exception exception) {
            markEmailFailed(request);

            LOGGER.error(
                    "Additional-document email delivery failed "
                            + "for request {}.",
                    requestId,
                    exception
            );
        }
    }

    private void markEmailFailed(
            ErpApplicationDocumentRequest request
    ) {
        request.setEmailStatus(
                ErpApplicationDocumentRequest
                        .EmailStatus.FAILED
        );
        request.setEmailSentAt(null);
        request.setUpdatedAt(
                LocalDateTime.now()
        );

        documentRequestRepository.save(request);
    }

    private boolean isExpired(
            ErpApplicationDocumentRequest request,
            LocalDateTime now
    ) {
        return request.getUploadTokenExpiresAt() != null
                && !request.getUploadTokenExpiresAt()
                .isAfter(now)
                || request.getUploadDeadline() != null
                && !request.getUploadDeadline()
                .isAfter(now);
    }

    private boolean tokenMatches(
            String rawToken,
            String storedHash
    ) {
        if (rawToken == null
                || rawToken.isBlank()
                || storedHash == null
                || storedHash.isBlank()) {
            return false;
        }

        byte[] expected =
                storedHash.trim()
                        .toLowerCase()
                        .getBytes(
                                StandardCharsets.US_ASCII
                        );

        byte[] actual =
                sha256Hex(rawToken)
                        .getBytes(
                                StandardCharsets.US_ASCII
                        );

        return MessageDigest.isEqual(
                expected,
                actual
        );
    }

    private String sha256Hex(
            String value
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return HexFormat.of()
                    .formatHex(
                            digest.digest(
                                    value.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            )
                    );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available.",
                    exception
            );
        }
    }
}
