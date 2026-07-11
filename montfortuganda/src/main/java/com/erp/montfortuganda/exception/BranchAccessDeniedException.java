package com.erp.montfortuganda.exception;

public class BranchAccessDeniedException extends RuntimeException {
    public BranchAccessDeniedException(String message) {
        super(message);
    }
}