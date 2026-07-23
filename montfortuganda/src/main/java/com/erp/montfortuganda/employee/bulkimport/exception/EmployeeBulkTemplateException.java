package com.erp.montfortuganda.employee.bulkimport.exception;

public class EmployeeBulkTemplateException
        extends RuntimeException {

    public EmployeeBulkTemplateException(String message) {
        super(message);
    }

    public EmployeeBulkTemplateException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}