package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.notification.service.EmailService;
import com.erp.montfortuganda.scholarship.entity.ErpScholarshipApplication;
import com.erp.montfortuganda.scholarship.entity.ErpScholarshipHistory;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipApplicationRepository;
import com.erp.montfortuganda.scholarship.repository.ErpScholarshipHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Sends scholarship application link emails only after the transaction that
 * issued/reissued the token has committed successfully.
 */
@Component
public class ScholarshipApplicationLinkEmailListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ScholarshipApplicationLinkEmailListener.class
            );

    private final ErpScholarshipApplicationRepository
            scholarshipApplicationRepository;

    private final EmailService emailService;

    private final ErpScholarshipHistoryRepository
            scholarshipHistoryRepository;

    public ScholarshipApplicationLinkEmailListener(
            ErpScholarshipApplicationRepository
                    scholarshipApplicationRepository,
            ErpScholarshipHistoryRepository
                    scholarshipHistoryRepository,
            EmailService emailService
    ) {
        this.scholarshipApplicationRepository =
                scholarshipApplicationRepository;
        this.scholarshipHistoryRepository =
                scholarshipHistoryRepository;
        this.emailService = emailService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void handleScholarshipApplicationLinkEmailRequested(
            ScholarshipApplicationLinkEmailRequestedEvent event
    ) {
        if (event == null) {
            return;
        }

        Long scholarshipApplicationId =
                event.getScholarshipApplicationId();

        ErpScholarshipApplication scholarship =
                scholarshipApplicationRepository
                        .findById(
                                scholarshipApplicationId
                        )
                        .orElse(null);

        if (scholarship == null) {
            LOGGER.error(
                    "Scholarship link email could not be sent because "
                            + "scholarship application {} was not found.",
                    scholarshipApplicationId
            );
            return;
        }

        if (!Boolean.TRUE.equals(
                scholarship.getActive()
        )) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (scholarship.getTokenExpiresAt() == null
                || !scholarship.getTokenExpiresAt()
                .isAfter(now)) {
            scholarship.setStatus(
                    "EXPIRED"
            );
            scholarship.setUpdatedAt(now);

            scholarshipApplicationRepository.save(
                    scholarship
            );

            LOGGER.warn(
                    "Scholarship application link email was not sent for "
                            + "{} because the token had expired.",
                    scholarshipApplicationId
            );
            return;
        }

        if (scholarship.getTokenUsedAt() != null) {
            LOGGER.warn(
                    "Scholarship application link email was not sent for "
                            + "{} because the token has already been used.",
                    scholarshipApplicationId
            );
            return;
        }

        if (!tokenMatches(
                event.getRawToken(),
                scholarship.getPublicTokenHash()
        )) {
            scholarship.setStatus(
                    "LINK_EMAIL_FAILED"
            );
            scholarship.setUpdatedAt(now);

            scholarshipApplicationRepository.save(
                    scholarship
            );

            LOGGER.error(
                    "Scholarship application link email was not sent for "
                            + "{} because token validation failed.",
                    scholarshipApplicationId
            );
            return;
        }

        if (scholarship.getApplication() == null) {
            scholarship.setStatus(
                    "LINK_EMAIL_FAILED"
            );
            scholarship.setUpdatedAt(now);

            scholarshipApplicationRepository.save(
                    scholarship
            );

            LOGGER.error(
                    "Scholarship application link email was not sent for "
                            + "{} because its admission application is missing.",
                    scholarshipApplicationId
            );
            return;
        }

        try {
            BigDecimal amountRequested =
                    scholarshipHistoryRepository
                            .findFirstByScholarshipApplicationScholarshipAppIdOrderByScholarshipHistoryIdDesc(
                                    scholarship.getScholarshipAppId()
                            )
                            .map(ErpScholarshipHistory::getAmountRequestedUgx)
                            .orElse(null);

            emailService.sendScholarshipApplicationLink(
                    scholarship.getApplication(),
                    event.getRawToken(),
                    scholarship.getTokenExpiresAt(),
                    amountRequested
            );

            scholarship.setStatus(
                    "LINK_SENT"
            );
            scholarship.setUpdatedAt(
                    LocalDateTime.now()
            );

            scholarshipApplicationRepository.save(
                    scholarship
            );

            LOGGER.info(
                    "Scholarship application link email marked LINK_SENT "
                            + "for scholarship application {}.",
                    scholarshipApplicationId
            );

        } catch (Exception exception) {
            scholarship.setStatus(
                    "LINK_EMAIL_FAILED"
            );
            scholarship.setUpdatedAt(
                    LocalDateTime.now()
            );

            scholarshipApplicationRepository.save(
                    scholarship
            );

            LOGGER.error(
                    "Scholarship application link email delivery failed "
                            + "for scholarship application {}.",
                    scholarshipApplicationId,
                    exception
            );
        }
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
