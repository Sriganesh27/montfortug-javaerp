package com.erp.montfortuganda.student.bulkimport.excel;

import com.erp.montfortuganda.student.entity.ErpParent.FeeResponsibility;
import com.erp.montfortuganda.student.entity.ErpParent.PreferredContact;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment.AdmissionType;
import com.erp.montfortuganda.student.entity.ErpStudentMedical.BloodGroup;
import com.erp.montfortuganda.student.enums.StudentGender;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Converts user-friendly Student Excel values into backend-supported types.
 *
 * This parser does not persist Student data. All values are parsed only after
 * the raw Excel row has been read.
 *
 * Invalid values throw IllegalArgumentException so the bulk validator can:
 *
 * 1. reject only the invalid Student row;
 * 2. write ENTER VALID DATA into the corrected workbook;
 * 3. continue processing the remaining rows.
 */
@Component
public class StudentExcelValueParser {

    private static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ISO_LOCAL_DATE;

    // =====================================================================
    // TEXT
    // =====================================================================

    /**
     * Normalizes an optional text value.
     *
     * Blank values and ENTER VALID DATA are treated as unresolved values.
     */
    public String nullableText(
            String value
    ) {
        String normalized =
                trimToNull(value);

        if (
                normalized == null
                        || isCorrectionMarker(normalized)
        ) {
            return null;
        }

        return normalized;
    }

    /**
     * Normalizes a mandatory text value.
     */
    public String requiredText(
            String value,
            String fieldName
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return normalized;
    }

    // =====================================================================
    // INTEGER VALUES
    // =====================================================================

    /**
     * Parses an optional whole number.
     */
    public Integer nullableInteger(
            String value,
            String fieldName
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        try {
            return Integer.valueOf(
                    normalized
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid whole number"
            );
        }
    }

    /**
     * Parses a required whole number.
     */
    public int requiredInteger(
            String value,
            String fieldName
    ) {
        Integer parsed =
                nullableInteger(
                        value,
                        fieldName
                );

        if (parsed == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return parsed;
    }

    // =====================================================================
    // DATES
    // =====================================================================

    /**
     * Accepts a date written strictly as YYYY-MM-DD.
     *
     * The date remains a String in StudentBulkImportRow and becomes a
     * LocalDate only through this method.
     */
    public LocalDate nullableDate(
            String value,
            String fieldName
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        try {
            return LocalDate.parse(
                    normalized,
                    ISO_DATE
            );
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    fieldName
                            + " must use YYYY-MM-DD format"
            );
        }
    }

    /**
     * Parses a mandatory date.
     */
    public LocalDate requiredDate(
            String value,
            String fieldName
    ) {
        LocalDate parsed =
                nullableDate(
                        value,
                        fieldName
                );

        if (parsed == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return parsed;
    }

    // =====================================================================
    // STUDENT GENDER
    // =====================================================================

    public StudentGender nullableGender(
            String value
    ) {
        String normalized =
                normalizeEnumValue(value);

        if (normalized == null) {
            return null;
        }

        try {
            return StudentGender.valueOf(
                    normalized
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Gender must be MALE, FEMALE or OTHER"
            );
        }
    }

    public StudentGender requiredGender(
            String value
    ) {
        StudentGender gender =
                nullableGender(value);

        if (gender == null) {
            throw new IllegalArgumentException(
                    "Gender is required"
            );
        }

        return gender;
    }

    // =====================================================================
    // ADMISSION TYPE
    // =====================================================================

    public AdmissionType nullableAdmissionType(
            String value
    ) {
        String normalized =
                normalizeEnumValue(value);

        if (normalized == null) {
            return null;
        }

        try {
            return AdmissionType.valueOf(
                    normalized
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Admission Type must be NEW, TRANSFER or READMISSION"
            );
        }
    }

    public AdmissionType requiredAdmissionType(
            String value
    ) {
        AdmissionType admissionType =
                nullableAdmissionType(value);

        if (admissionType == null) {
            throw new IllegalArgumentException(
                    "Admission Type is required"
            );
        }

        return admissionType;
    }

    // =====================================================================
    // PREFERRED CONTACT
    // =====================================================================

    public PreferredContact nullablePreferredContact(
            String value
    ) {
        String normalized =
                normalizeEnumValue(value);

        if (normalized == null) {
            return null;
        }

        try {
            return PreferredContact.valueOf(
                    normalized
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Preferred Contact must be FATHER, MOTHER or GUARDIAN"
            );
        }
    }

    public PreferredContact requiredPreferredContact(
            String value
    ) {
        PreferredContact preferredContact =
                nullablePreferredContact(value);

        if (preferredContact == null) {
            throw new IllegalArgumentException(
                    "Preferred Contact is required"
            );
        }

        return preferredContact;
    }

    // =====================================================================
    // FEE RESPONSIBILITY
    // =====================================================================

    public FeeResponsibility nullableFeeResponsibility(
            String value
    ) {
        String normalized =
                normalizeEnumValue(value);

        if (normalized == null) {
            return null;
        }

        try {
            return FeeResponsibility.valueOf(
                    normalized
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Fee Responsibility must be FATHER, MOTHER, "
                            + "GUARDIAN or SPONSOR"
            );
        }
    }

    public FeeResponsibility requiredFeeResponsibility(
            String value
    ) {
        FeeResponsibility feeResponsibility =
                nullableFeeResponsibility(value);

        if (feeResponsibility == null) {
            throw new IllegalArgumentException(
                    "Fee Responsibility is required"
            );
        }

        return feeResponsibility;
    }

    // =====================================================================
    // BLOOD GROUP
    // =====================================================================

    /**
     * Supports both user-friendly symbols and enum names:
     *
     * A+       -> A_PLUS
     * A-       -> A_MINUS
     * AB+      -> AB_PLUS
     * O-       -> O_MINUS
     * UNKNOWN  -> UNKNOWN
     */
    public BloodGroup nullableBloodGroup(
            String value
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        String bloodGroup =
                normalized
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "");

        return switch (bloodGroup) {
            case "A+", "A_PLUS" ->
                    BloodGroup.A_PLUS;

            case "A-", "A_MINUS" ->
                    BloodGroup.A_MINUS;

            case "B+", "B_PLUS" ->
                    BloodGroup.B_PLUS;

            case "B-", "B_MINUS" ->
                    BloodGroup.B_MINUS;

            case "AB+", "AB_PLUS" ->
                    BloodGroup.AB_PLUS;

            case "AB-", "AB_MINUS" ->
                    BloodGroup.AB_MINUS;

            case "O+", "O_PLUS" ->
                    BloodGroup.O_PLUS;

            case "O-", "O_MINUS" ->
                    BloodGroup.O_MINUS;

            case "UNKNOWN", "NOT_KNOWN", "NOTKNOWN" ->
                    BloodGroup.UNKNOWN;

            default ->
                    throw new IllegalArgumentException(
                            "Blood Group must be one of: "
                                    + "A+, A-, B+, B-, AB+, AB-, "
                                    + "O-, O+ or UNKNOWN"
                    );
        };
    }

    // =====================================================================
    // YES / NO
    // =====================================================================

    /**
     * Accepts these user-friendly Excel values:
     *
     * YES, NO, TRUE, FALSE, 1 and 0.
     */
    public Boolean nullableYesNo(
            String value,
            String fieldName
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        return switch (
                normalized.toUpperCase(Locale.ROOT)
                ) {
            case "YES", "TRUE", "1" ->
                    Boolean.TRUE;

            case "NO", "FALSE", "0" ->
                    Boolean.FALSE;

            default ->
                    throw new IllegalArgumentException(
                            fieldName + " must be YES or NO"
                    );
        };
    }

    /**
     * Parses a mandatory Yes/No value.
     */
    public boolean requiredYesNo(
            String value,
            String fieldName
    ) {
        Boolean parsed =
                nullableYesNo(
                        value,
                        fieldName
                );

        if (parsed == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return parsed;
    }

    // =====================================================================
    // NORMALIZATION
    // =====================================================================

    /**
     * Converts user-friendly enum text:
     *
     * Readmission       -> READMISSION
     * Fee Sponsor       -> FEE_SPONSOR
     * Full Time         -> FULL_TIME
     */
    public String normalizeEnumValue(
            String value
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        return normalized
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replaceAll("\\s+", "_");
    }

    /**
     * Produces a normalized key for reference-data lookup.
     *
     * Examples:
     *
     * Senior Secondary -> SENIORSECONDARY
     * Senior-Secondary -> SENIORSECONDARY
     * Senior_Secondary -> SENIORSECONDARY
     */
    public String normalizeLookupKey(
            String value
    ) {
        String normalized =
                nullableText(value);

        if (normalized == null) {
            return null;
        }

        return normalized
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s_-]+", "");
    }

    /**
     * Returns true when the cell contains the correction marker.
     */
    public boolean isCorrectionMarker(
            String value
    ) {
        return value != null
                && StudentExcelHeaders.ENTER_VALID_DATA
                .equalsIgnoreCase(
                        value.trim()
                );
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}