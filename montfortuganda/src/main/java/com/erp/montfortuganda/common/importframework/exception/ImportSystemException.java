package com.erp.montfortuganda.common.importframework.exception;

import lombok.Getter;

@Getter
public class ImportSystemException extends RuntimeException {
    private final String errorCode;

    public ImportSystemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ImportSystemException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
