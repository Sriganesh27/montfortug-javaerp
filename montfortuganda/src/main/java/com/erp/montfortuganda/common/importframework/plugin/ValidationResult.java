package com.erp.montfortuganda.common.importframework.plugin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValidationResult {
    private final boolean success;
    private final boolean skipRow;
    private final List<ValidationError> errors;
    private final List<ValidationWarning> warnings;

    @Getter
    @Builder
    public static class ValidationError {
        private final String columnName;
        private final String cellValue;
        private final String errorCode;
        private final String message;
        private final String suggestedFix;
    }

    @Getter
    @Builder
    public static class ValidationWarning {
        private final String columnName;
        private final String cellValue;
        private final String message;
    }
}