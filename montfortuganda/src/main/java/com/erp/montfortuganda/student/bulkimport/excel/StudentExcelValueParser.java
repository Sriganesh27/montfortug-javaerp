package com.erp.montfortuganda.student.bulkimport.excel;

import com.erp.montfortuganda.student.entity.ErpParent.FeeResponsibility;
import com.erp.montfortuganda.student.entity.ErpParent.PreferredContact;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment.AdmissionType;
import com.erp.montfortuganda.student.entity.ErpStudentMedical.BloodGroup;
import com.erp.montfortuganda.student.enums.StudentGender;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern NUMERIC_DATE_PATTERN =
            Pattern.compile(
                    "^(\\d{1,4})[./-](\\d{1,2})[./-](\\d{1,4})$"
            );

    private static final Pattern COMPACT_DATE_PATTERN =
            Pattern.compile(
                    "^(\\d{4})(\\d{2})(\\d{2})$"
            );

    private static final Pattern EXCEL_SERIAL_PATTERN =
            Pattern.compile(
                    "^\\d{5}(?:\\.\\d+)?$"
            );

    private static final Pattern TIME_SUFFIX_PATTERN =
            Pattern.compile(
                    "(?i)[T\\s]+\\d{1,2}:\\d{2}"
                            + "(?::\\d{2}(?:\\.\\d+)?)?"
                            + "(?:\\s*[AP]M)?$"
            );

    private static final Pattern ORDINAL_DAY_PATTERN =
            Pattern.compile(
                    "(?i)\\b(\\d{1,2})(ST|ND|RD|TH)\\b"
            );

    private static final List<DateTimeFormatter> TEXT_DATE_FORMATTERS =
            List.of(
                    strictFormatter("d MMM uuuu"),
                    strictFormatter("d MMMM uuuu"),
                    strictFormatter("MMM d uuuu"),
                    strictFormatter("MMMM d uuuu"),
                    strictFormatter("d-MMM-uuuu"),
                    strictFormatter("d-MMMM-uuuu"),
                    strictFormatter("MMM-d-uuuu"),
                    strictFormatter("MMMM-d-uuuu")
            );

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
     * Parses common Student workbook date formats.
     *
     * <p>Supported examples:</p>
     *
     * <ul>
     *     <li>2017-07-31, 2017/07/31, 2017.07.31</li>
     *     <li>7/31/2017, 07/31/2017, 7-31-2017</li>
     *     <li>31/07/2017, 31-07-2017, 31.07.2017</li>
     *     <li>31 Jul 2017, 31 July 2017</li>
     *     <li>Jul 31, 2017, July 31, 2017</li>
     *     <li>20170731</li>
     *     <li>Excel date serial values</li>
     *     <li>The same values with a trailing time component</li>
     * </ul>
     *
     * <p>For an ambiguous numeric date where both the first and second
     * numbers are at most 12, month/day/year is attempted first. This keeps
     * 11/2/2016 consistent with a workbook containing an unambiguous value
     * such as 7/31/2017.</p>
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

        String dateText =
                normalizeDateText(
                        normalized
                );

        LocalDate parsed =
                parseExcelSerial(dateText);

        if (parsed == null) {
            parsed = parseCompactDate(dateText);
        }

        if (parsed == null) {
            parsed = parseNumericSeparatedDate(dateText);
        }

        if (parsed == null) {
            parsed = parseTextDate(dateText);
        }

        if (parsed != null) {
            return parsed;
        }

        throw new IllegalArgumentException(
                fieldName
                        + " contains an invalid date. Accepted examples: "
                        + "2017-07-31, 7/31/2017, 31/07/2017, "
                        + "31-07-2017, 31 Jul 2017 or July 31, 2017."
        );
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

    private LocalDate parseNumericSeparatedDate(
            String value
    ) {
        Matcher matcher =
                NUMERIC_DATE_PATTERN.matcher(value);

        if (!matcher.matches()) {
            return null;
        }

        String firstToken = matcher.group(1);
        int first = Integer.parseInt(firstToken);
        int second = Integer.parseInt(matcher.group(2));
        String thirdToken = matcher.group(3);
        int third = Integer.parseInt(thirdToken);

        if (firstToken.length() == 4) {
            return createDate(
                    first,
                    second,
                    third
            );
        }

        if (
                thirdToken.length() != 2
                        && thirdToken.length() != 4
        ) {
            return null;
        }

        int year =
                normalizeYear(
                        third,
                        thirdToken.length()
                );

        if (first > 12 && second <= 12) {
            return createDate(
                    year,
                    second,
                    first
            );
        }

        if (second > 12 && first <= 12) {
            return createDate(
                    year,
                    first,
                    second
            );
        }

        if (first <= 12 && second <= 12) {
            LocalDate monthFirst =
                    createDate(
                            year,
                            first,
                            second
                    );

            if (monthFirst != null) {
                return monthFirst;
            }

            return createDate(
                    year,
                    second,
                    first
            );
        }

        return null;
    }

    private LocalDate parseCompactDate(
            String value
    ) {
        Matcher matcher =
                COMPACT_DATE_PATTERN.matcher(value);

        if (!matcher.matches()) {
            return null;
        }

        return createDate(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        );
    }

    private LocalDate parseTextDate(
            String value
    ) {
        for (
                DateTimeFormatter formatter
                : TEXT_DATE_FORMATTERS
        ) {
            try {
                return LocalDate.parse(
                        value,
                        formatter
                );
            } catch (DateTimeParseException ignored) {
                // Try the next supported text format.
            }
        }

        return null;
    }

    private LocalDate parseExcelSerial(
            String value
    ) {
        if (
                !EXCEL_SERIAL_PATTERN
                        .matcher(value)
                        .matches()
        ) {
            return null;
        }

        try {
            double serial =
                    Double.parseDouble(value);

            /*
             * Modern Student DOBs represented as Excel serial dates are
             * normally above 20,000. This prevents a plain year such as
             * 2017 from being interpreted as an Excel serial date.
             */
            if (
                    serial < 20_000
                            || serial > 100_000
                            || !DateUtil.isValidExcelDate(serial)
            ) {
                return null;
            }

            return DateUtil
                    .getLocalDateTime(serial)
                    .toLocalDate();
        } catch (
                NumberFormatException
                | DateTimeException exception
        ) {
            return null;
        }
    }

    private LocalDate createDate(
            int year,
            int month,
            int day
    ) {
        try {
            return LocalDate.of(
                    year,
                    month,
                    day
            );
        } catch (DateTimeException exception) {
            return null;
        }
    }

    private int normalizeYear(
            int year,
            int tokenLength
    ) {
        if (tokenLength == 4) {
            return year;
        }

        return year <= 49
                ? 2000 + year
                : 1900 + year;
    }

    private String normalizeDateText(
            String value
    ) {
        String normalized =
                value.trim()
                        .replace(',', ' ')
                        .replaceAll("\\s+", " ");

        Matcher ordinalMatcher =
                ORDINAL_DAY_PATTERN.matcher(normalized);

        normalized =
                ordinalMatcher.replaceAll("$1");

        normalized =
                TIME_SUFFIX_PATTERN
                        .matcher(normalized)
                        .replaceFirst("")
                        .trim();

        return normalized;
    }

    private static DateTimeFormatter strictFormatter(
            String pattern
    ) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH)
                .withResolverStyle(
                        ResolverStyle.STRICT
                );
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