package com.erp.montfortuganda.employee.bulkimport.excel;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.Gender;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Converts user-friendly Employee Excel values into backend-supported types.

 * This class does not save anything and does not generate random employee data.
 */
@Component
public class EmployeeExcelValueParser {

    private static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Normalizes a normal text field.
     *
     * Blank and ENTER VALID DATA are treated as unresolved values.
     */
    public String nullableText(String value) {
        String normalized = trimToNull(value);

        if (normalized == null || isCorrectionMarker(normalized)) {
            return null;
        }

        return normalized;
    }

    /**
     * Reads a required text field.
     */
    public String requiredText(String value, String fieldName) {
        String normalized = nullableText(value);

        if (normalized == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return normalized;
    }

    /**
     * Accepts a date written as YYYY-MM-DD.
     *
     * The generic SAX Excel reader already formats real Excel date cells
     * into text. This parser validates the resulting text strictly.
     */
    public LocalDate nullableDate(String value, String fieldName) {
        String normalized = nullableText(value);

        if (normalized == null) {
            return null;
        }

        try {
            return LocalDate.parse(normalized, ISO_DATE);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    fieldName + " must use YYYY-MM-DD format"
            );
        }
    }

    public LocalDate requiredDate(String value, String fieldName) {
        LocalDate date = nullableDate(value, fieldName);

        if (date == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return date;
    }

    public Gender nullableGender(String value) {
        String normalized = normalizeEnumValue(value);

        if (normalized == null) {
            return null;
        }

        try {
            return Gender.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Gender must be MALE, FEMALE or OTHER"
            );
        }
    }

    public EmployeeCategory nullableEmployeeCategory(String value) {
        String normalized = normalizeEnumValue(value);

        if (normalized == null) {
            return null;
        }

        try {
            return EmployeeCategory.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Employee Category must be one of: "
                            + "TEACHING, NON_TEACHING, "
                            + "MANAGEMENT_TEACHING, "
                            + "MANAGEMENT_NON_TEACHING or SUPPORT_STAFF"
            );
        }
    }

    public EmployeeType requiredEmployeeType(String value) {
        String normalized = normalizeEnumValue(value);

        if (normalized == null) {
            throw new IllegalArgumentException(
                    "Employee Type is required"
            );
        }

        try {
            return EmployeeType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Employee Type must be one of: "
                            + "PERMANENT, CONTRACT, TEMPORARY, PART_TIME, "
                            + "INTERN, VOLUNTEER or HONORY"
            );
        }
    }

    public EmploymentMode requiredEmploymentMode(String value) {
        String normalized = normalizeEnumValue(value);

        if (normalized == null) {
            throw new IllegalArgumentException(
                    "Employment Mode is required"
            );
        }

        try {
            return EmploymentMode.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Employment Mode must be one of: "
                            + "FULL_TIME, PART_TIME, REMOTE or ON_CALL"
            );
        }
    }

    /**
     * Accepts user-friendly Excel values:
     * YES, NO, TRUE, FALSE, 1 and 0.
     */
    public Boolean nullableYesNo(String value, String fieldName) {
        String normalized = nullableText(value);

        if (normalized == null) {
            return null;
        }

        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "YES", "TRUE", "1" -> Boolean.TRUE;
            case "NO", "FALSE", "0" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException(
                    fieldName + " must be YES or NO"
            );
        };
    }

    public boolean requiredYesNo(String value, String fieldName) {
        Boolean parsed = nullableYesNo(value, fieldName);

        if (parsed == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return parsed;
    }

    /**
     * Converts values such as:
     *
     * Full Time              -> FULL_TIME
     * Non Teaching           -> NON_TEACHING
     * Management Teaching    -> MANAGEMENT_TEACHING
     */
    public String normalizeEnumValue(String value) {
        String normalized = nullableText(value);

        if (normalized == null) {
            return null;
        }

        return normalized
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replaceAll("\\s+", "_");
    }

    public String normalizeLookupKey(String value) {
        String normalized = nullableText(value);

        if (normalized == null) {
            return null;
        }

        return normalized
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    public boolean isCorrectionMarker(String value) {
        return value != null
                && EmployeeExcelHeaders.ENTER_VALID_DATA
                .equalsIgnoreCase(value.trim());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}