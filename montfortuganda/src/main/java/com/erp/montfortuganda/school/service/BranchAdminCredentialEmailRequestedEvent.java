package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.service.model.BranchAdminCredentials;

import java.util.Objects;

/**
 * Internal after-commit request for Branch Admin credential delivery.
 */
public final class BranchAdminCredentialEmailRequestedEvent {

    private final Integer branchId;
    private final BranchAdminCredentials credentials;
    private final boolean resent;

    public BranchAdminCredentialEmailRequestedEvent(
            Integer branchId,
            BranchAdminCredentials credentials,
            boolean resent
    ) {
        this.branchId =
                Objects.requireNonNull(
                        branchId,
                        "Branch ID is required."
                );

        this.credentials =
                Objects.requireNonNull(
                        credentials,
                        "Branch Admin credentials are required."
                );

        this.resent = resent;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public BranchAdminCredentials getCredentials() {
        return credentials;
    }

    public boolean isResent() {
        return resent;
    }

    @Override
    public String toString() {
        return "BranchAdminCredentialEmailRequestedEvent{"
                + "branchId="
                + branchId
                + ", credentials="
                + credentials
                + ", resent="
                + resent
                + '}';
    }
}
