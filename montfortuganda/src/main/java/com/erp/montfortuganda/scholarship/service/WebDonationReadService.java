package com.erp.montfortuganda.scholarship.service;

import com.erp.montfortuganda.exception.ExternalDonationUnavailableException;
import com.erp.montfortuganda.scholarship.entity.WebDonation;
import com.erp.montfortuganda.scholarship.repository.WebDonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebDonationReadService {

    private final WebDonationRepository donationRepository;

    @Value("${erp.donations.external-enabled:false}")
    private boolean externalDonationDatabaseEnabled;

    public List<WebDonation> findAll() {
        requireExternalDatabaseEnabled();

        try {
            return donationRepository.findAll();
        } catch (
                DataAccessException
                | CannotCreateTransactionException exception
        ) {
            throw unavailable(exception);
        }
    }

    public List<WebDonation> findAllById(
            Collection<Long> donationIds
    ) {
        if (donationIds == null || donationIds.isEmpty()) {
            return List.of();
        }

        requireExternalDatabaseEnabled();

        try {
            return donationRepository.findAllById(
                    donationIds
            );
        } catch (
                DataAccessException
                | CannotCreateTransactionException exception
        ) {
            throw unavailable(exception);
        }
    }

    public Optional<WebDonation> findById(
            Long donationId
    ) {
        requireExternalDatabaseEnabled();

        try {
            return donationRepository.findById(
                    donationId
            );
        } catch (
                DataAccessException
                | CannotCreateTransactionException exception
        ) {
            throw unavailable(exception);
        }
    }

    private void requireExternalDatabaseEnabled() {
        if (!externalDonationDatabaseEnabled) {
            throw new ExternalDonationUnavailableException(
                    "Donation information is temporarily unavailable.",
                    null
            );
        }
    }

    private ExternalDonationUnavailableException unavailable(
            RuntimeException cause
    ) {
        return new ExternalDonationUnavailableException(
                "Donation information is temporarily unavailable.",
                cause
        );
    }
}
