package com.erp.montfortuganda.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@SuppressWarnings("unused") // Tells the IDE that Spring calls these via reflection
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------------------------------------------------------
    // 1. DTO Validation Errors (@Valid in Controllers)
    // -------------------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (org.springframework.validation.FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Validation failed");
        errorResponse.put("errors", fieldErrors); // Return structured field-level errors to the frontend

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // -------------------------------------------------------------------------
    // 2. Entity Constraint Violations (@NotNull, @Size, @DecimalMin on Entities)
    // -------------------------------------------------------------------------
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(jakarta.validation.ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (jakarta.validation.ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            // Extract the simple field name (e.g., "testScore")
            String fieldName = propertyPath.contains(".") ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : propertyPath;
            fieldErrors.put(fieldName, violation.getMessage());
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Database constraint violation");
        errorResponse.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // -------------------------------------------------------------------------
    // 3. Database Level Violations (e.g., Unique Key conflicts)
    // -------------------------------------------------------------------------
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException ex) {
        logger.error("Data integrity violation: ", ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "A database constraint was violated (e.g., duplicate entry or missing required data).");
    }

    // -------------------------------------------------------------------------
    // 4. Standard Application Errors
    // -------------------------------------------------------------------------
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource not found: " + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid input: " + ex.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage());
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        // Return a standard 404 instead of a 500 JSON for missing images/css
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found");
    }

    // -------------------------------------------------------------------------
    // 5. Fallback for absolutely everything else
    // -------------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        // Log the actual error to the console for debugging
        logger.error("Unhandled server exception occurred: ", ex);

        // Do not expose stack traces to the frontend. Use a generic message.
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred.");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return ResponseEntity.status(status).body(errorResponse);
    }
}