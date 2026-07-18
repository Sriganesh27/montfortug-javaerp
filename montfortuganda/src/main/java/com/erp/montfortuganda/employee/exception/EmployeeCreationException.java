package com.erp.montfortuganda.employee.exception;

import com.erp.montfortuganda.employee.enums.EmployeeCreationStage;
import org.springframework.http.HttpStatus;

public class EmployeeCreationException
        extends RuntimeException {

    private final EmployeeCreationStage stage;

    private final String errorCode;

    private final String field;

    private final HttpStatus httpStatus;

    public EmployeeCreationException(
            EmployeeCreationStage stage,
            String errorCode,
            String message,
            String field,
            HttpStatus httpStatus
    ) {
        super(message);

        this.stage = stage;
        this.errorCode = errorCode;
        this.field = field;
        this.httpStatus = httpStatus;
    }

    public EmployeeCreationException(
            EmployeeCreationStage stage,
            String errorCode,
            String message,
            String field,
            HttpStatus httpStatus,
            Throwable cause
    ) {
        super(message, cause);

        this.stage = stage;
        this.errorCode = errorCode;
        this.field = field;
        this.httpStatus = httpStatus;
    }

    public static EmployeeCreationException badRequest(
            EmployeeCreationStage stage,
            String errorCode,
            String message,
            String field
    ) {
        return new EmployeeCreationException(
                stage,
                errorCode,
                message,
                field,
                HttpStatus.BAD_REQUEST
        );
    }

    public static EmployeeCreationException conflict(
            EmployeeCreationStage stage,
            String errorCode,
            String message,
            String field
    ) {
        return new EmployeeCreationException(
                stage,
                errorCode,
                message,
                field,
                HttpStatus.CONFLICT
        );
    }

    public static EmployeeCreationException internalError(
            EmployeeCreationStage stage,
            String errorCode,
            String message,
            String field,
            Throwable cause
    ) {
        return new EmployeeCreationException(
                stage,
                errorCode,
                message,
                field,
                HttpStatus.INTERNAL_SERVER_ERROR,
                cause
        );
    }

    public EmployeeCreationStage getStage() {
        return stage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getField() {
        return field;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}