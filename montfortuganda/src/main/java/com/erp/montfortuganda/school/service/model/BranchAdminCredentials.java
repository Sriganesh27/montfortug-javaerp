package com.erp.montfortuganda.school.service.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Internal result created when a Branch Admin account is provisioned.
 *
 * The temporary password must exist only in application memory and must
 * never be stored as plain text, logged, or returned by a normal API.
 */
public final class BranchAdminCredentials {

    private final Integer userId;
    private final Integer branchId;
    private final String username;
    private final String temporaryPassword;
    private final LocalDateTime expiresAt;
    private final Integer credentialVersion;

    public BranchAdminCredentials(
            Integer userId,
            Integer branchId,
            String username,
            String temporaryPassword,
            LocalDateTime expiresAt,
            Integer credentialVersion
    ) {
        this.userId =
                Objects.requireNonNull(
                        userId,
                        "User ID is required."
                );

        this.branchId =
                Objects.requireNonNull(
                        branchId,
                        "Branch ID is required."
                );

        this.username =
                requireText(
                        username,
                        "Username is required."
                );

        this.temporaryPassword =
                requireText(
                        temporaryPassword,
                        "Temporary password is required."
                );

        this.expiresAt =
                Objects.requireNonNull(
                        expiresAt,
                        "Temporary password expiry is required."
                );

        this.credentialVersion =
                Objects.requireNonNull(
                        credentialVersion,
                        "Credential version is required."
                );

        if (credentialVersion < 1) {
            throw new IllegalArgumentException(
                    "Credential version must be positive."
            );
        }
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public String getUsername() {
        return username;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public Integer getCredentialVersion() {
        return credentialVersion;
    }

    @Override
    public String toString() {
        return "BranchAdminCredentials{"
                + "userId="
                + userId
                + ", branchId="
                + branchId
                + ", username='"
                + username
                + '\''
                + ", temporaryPassword='[PROTECTED]'"
                + ", expiresAt="
                + expiresAt
                + ", credentialVersion="
                + credentialVersion
                + '}';
    }

    private static String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}
