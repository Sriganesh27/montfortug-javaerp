package com.erp.montfortuganda.auth.entity;

/**
 * Tracks the lifecycle of temporary login credentials.
 */
public enum CredentialDeliveryStatus {

    /**
     * The account does not use temporary credentials.
     */
    NOT_REQUIRED,

    /**
     * Temporary credentials were generated but not yet emailed.
     */
    PENDING,

    /**
     * The credentials email was sent successfully.
     */
    SENT,

    /**
     * The credentials email could not be delivered.
     */
    FAILED,

    /**
     * A replacement temporary password was generated and sent.
     */
    RESENT,

    /**
     * The user changed the temporary password successfully.
     */
    ACCEPTED,

    /**
     * The temporary password expired before it was changed.
     */
    EXPIRED
}