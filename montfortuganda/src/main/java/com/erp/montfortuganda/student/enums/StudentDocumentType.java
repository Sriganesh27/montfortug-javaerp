package com.erp.montfortuganda.student.enums;

/**
 * Supported Student document categories.
 *
 * These values align with Admission documents so approved application
 * documents can later be transferred safely into the Student profile.
 *
 * The database column remains VARCHAR(100).
 */
public enum StudentDocumentType {

    PHOTO,
    BIRTH_CERTIFICATE,
    REPORT_CARD,
    TRANSFER_LETTER,
    PASSPORT,
    NATIONAL_ID,
    IMMUNIZATION_CARD,
    RECOMMENDATION_LETTER,
    MEDICAL_CERTIFICATE,
    OTHER
}