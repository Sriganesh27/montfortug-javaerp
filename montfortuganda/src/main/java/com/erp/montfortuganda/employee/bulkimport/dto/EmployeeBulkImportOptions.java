package com.erp.montfortuganda.employee.bulkimport.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Batch-level options selected in the future Employee Bulk Import UI.
 *
 * These options do not replace the row-level "Login Enabled" Excel value.
 */
@Getter
@Builder
public class EmployeeBulkImportOptions {

    @Builder.Default
    private final boolean createCredentials = false;

    @Builder.Default
    private final boolean sendEmail = false;

    /**
     * Optional role selected by the administrator.
     * Role validation will be added during account integration.
     */
    private final Long roleId;

    public void validate() {
        if (sendEmail && !createCredentials) {
            throw new IllegalArgumentException(
                    "Send Email requires Create Credentials"
            );
        }
    }
}