package com.erp.montfortuganda.common.importframework.lifecycle;

public enum ImportStatus {
    CREATED,
    QUEUED,
    INITIALIZING,
    VALIDATING_FILE,
    READING_ROWS,
    VALIDATING_ROWS,
    MAPPING,
    SAVING_BATCH,
    GENERATING_REPORT,
    NOTIFYING,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    FAILED,
    PAUSED,
    RESUMED,
    CANCELLED
}
