package com.erp.montfortuganda.common.importframework.lifecycle;

public enum ImportMode {
    INSERT,
    UPDATE,
    UPSERT,
    VALIDATE_ONLY,
    RETRY_FAILED_ROWS
}