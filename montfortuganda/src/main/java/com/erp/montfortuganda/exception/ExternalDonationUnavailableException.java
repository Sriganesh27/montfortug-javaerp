package com.erp.montfortuganda.exception;

public class ExternalDonationUnavailableException
        extends RuntimeException {

    public ExternalDonationUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
