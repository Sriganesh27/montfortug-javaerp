package com.erp.montfortuganda.auth.dto;

import java.time.LocalDateTime;

/**
 * Authentication response returned for both normal login and
 * temporary-password login flows.
 */
public record AuthResponse(
        String token,
        String role,
        Integer branchId,
        Status status,
        String message,
        String passwordChangeToken,
        LocalDateTime temporaryPasswordExpiresAt
) {

    /**
     * Keeps the existing three-argument constructor compatible with the
     * current AuthController until that controller is updated.
     */
    public AuthResponse(
            String token,
            String role,
            Integer branchId
    ) {
        this(
                token,
                role,
                branchId,
                Status.AUTHENTICATED,
                "Login successful.",
                null,
                null
        );
    }

    public enum Status {
        AUTHENTICATED,
        PASSWORD_CHANGE_REQUIRED,
        TEMPORARY_PASSWORD_EXPIRED,
        ACCOUNT_DISABLED,
        INVALID_CREDENTIALS
    }
}