package com.erp.montfortuganda.common.importframework.plugin;

public interface ImportStrategyProvider {
    DuplicateStrategy getDuplicateStrategy();
    RetryStrategy getRetryStrategy();
    ValidationStrategy getValidationStrategy();
}
